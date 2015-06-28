package org.konzult.sling.utils.depgen.impl.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.konzult.sling.utils.depgen.DepGenConstants;
import org.konzult.sling.utils.depgen.api.handler.POMGenerator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component(
	name = "org.konzult.sling.utils.depgen.impl.component.POMGenerator",
	immediate = true,
	metatype = true,
	label = "DepGen pom generator"
)
@Service
@Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "POM Generator"),
	@Property(name = Constants.SERVICE_VENDOR, value = DepGenConstants.VENDOR),
	@Property(name = Constants.VISIBILITY_PRIVATE, value = "true")
})
public class POMGeneratorImpl implements POMGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(POMGeneratorImpl.class);
	
	private static final String META_INF = "META-INF";
	private static final String FN_POM_PROPERTIES = "pom.properties";
	
	private boolean includeVersionScope;
	
	BundleContext bctx;
	
	@Override
	public String generate(boolean includeVersionScope) throws IOException {
		
		this.includeVersionScope = includeVersionScope;
		
		Function<Bundle, String> extractProperties = (Bundle bundle) -> 
			{
				try {
					return extractResource(bundle.findEntries(META_INF, FN_POM_PROPERTIES, true));
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			};
			
		String dependencies = Arrays.asList(bctx.getBundles()).parallelStream()
			.map(extractProperties).collect(Collectors.joining());
			
		return fillTemplate(dependencies);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private String extractResource(final Enumeration enumResources) {
		if (enumResources == null) {
			return "";
		}
		
		Function<URL, String> handleEnumRes = (URL url) ->
			{
				try {
					return extractIds((url).openStream());
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			};
		
		return (String) Collections.list(enumResources).parallelStream()
					.map(handleEnumRes).collect(Collectors.joining());
	}
	
	private String extractIds(InputStream resStream) throws IOException, ParserConfigurationException {
		java.util.Properties props = new java.util.Properties();
		props.load(resStream);
		
		String artifactIdString = props.get(DepGenConstants.KEY_ARTIFACT_ID).toString();
		
		//skip this bundle?
		if (artifactIdString.equals(DepGenConstants.ARTIFACT_ID)) return "";
		
		//doc goes to the caller
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document xmlDoc = factory.newDocumentBuilder().newDocument();
        Element deps = xmlDoc.createElement("dependencies");
        Element dep = xmlDoc.createElement("dependency");
        deps.appendChild(dep);
        Element groupId = xmlDoc.createElement("groupId");
        groupId.setTextContent(props.get(DepGenConstants.KEY_GROUP_ID).toString());
        dep.appendChild(groupId);
        Element artifactId = xmlDoc.createElement("artifactId");
        artifactId.setTextContent(props.get(DepGenConstants.KEY_ARTIFACT_ID).toString());
        dep.appendChild(artifactId);
        if (includeVersionScope) {
        	Element version = xmlDoc.createElement("version");
        	version.setTextContent(props.get(DepGenConstants.KEY_VERSION).toString());
            dep.appendChild(version);
            Element scope = xmlDoc.createElement("scope");
            scope.setTextContent("provided");
            dep.appendChild(scope);
		}
		
		StringJoiner joiner = new StringJoiner("\n")
			.add("<dependency>")
			.add("<groupId>" + props.get(DepGenConstants.KEY_GROUP_ID) + "</groupId>")
			.add("<artifactId>" + artifactIdString + "</artifactId>");
		if (includeVersionScope) {
			joiner.add("<version>" + props.get(DepGenConstants.KEY_VERSION) + "</version>");
			joiner.add("<scope>provided</scope>");
		}
		joiner.add("</dependency>\n");
		
		LOGGER.debug(joiner.toString());
		
		return joiner.toString();
	}

	private String fillTemplate(String dependencies) throws IOException {
		InputStream inputStream = POMGeneratorImpl.class.getClassLoader()
													.getResourceAsStream(DepGenConstants.PATH_POM_TEMPLATE);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());
		String template = writer.toString();
		return template.replace(DepGenConstants.PLACEHOLDER, dependencies);
	}
	
	protected final void activate(final ComponentContext cctx) {
		bctx = cctx.getBundleContext();
	}
}