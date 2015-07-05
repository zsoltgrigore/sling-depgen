package org.konzult.sling.utils.depgen.impl.handler;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.felix.scr.annotations.Component;
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
import org.w3c.dom.Node;

@Component(
	name = "org.konzult.sling.utils.depgen.impl.component.POMGenerator",
	immediate = true,
	metatype = true,
	label = "DepGen pom generator"
)
@Service
@org.apache.felix.scr.annotations.Properties({
	@org.apache.felix.scr.annotations.Property(name = Constants.SERVICE_DESCRIPTION, value = "POM Generator"),
	@org.apache.felix.scr.annotations.Property(name = Constants.SERVICE_VENDOR, value = DepGenConstants.VENDOR),
	@org.apache.felix.scr.annotations.Property(name = Constants.VISIBILITY_PRIVATE, value = "true")
})
public class POMGeneratorImpl implements POMGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(POMGeneratorImpl.class);
	
	private static final String META_INF = "META-INF";
	private static final String FN_POM_PROPERTIES = "pom.properties";
	
	
	BundleContext bctx;
	
	@Override
	public Node generate(boolean includeVersionScope) throws IOException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			Document xmlDoc = factory.newDocumentBuilder().newDocument();
		
	        Element deps = xmlDoc.createElement("dependencies");
			
	        int numOfBundles = 0;
	        
			for (Bundle bundle : bctx.getBundles()) {
				@SuppressWarnings("rawtypes")
				Enumeration entries = bundle.findEntries(META_INF, FN_POM_PROPERTIES, true);
				while(entries != null && entries.hasMoreElements()) {
					Properties props = new java.util.Properties();
					props.load(((URL)entries.nextElement()).openStream());
					
					String artifactIdString = props.get(DepGenConstants.KEY_ARTIFACT_ID).toString();
					
					//skip this bundle?
					if (!artifactIdString.equals(DepGenConstants.ARTIFACT_ID)) {
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
				        numOfBundles++;
					}
				}
			}
			LOGGER.info("Written {} bundles as dependencies.", numOfBundles);
			
			return deps;
			
		} catch (ParserConfigurationException e) {
			LOGGER.error("Configureation error while parsing {}", e.getMessage(), e);
		}
		
		return null;
			
	}
	
	protected final void activate(final ComponentContext cctx) {
		bctx = cctx.getBundleContext();
	}
}