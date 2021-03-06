package org.konzult.sling.utils.depgen.impl.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.konzult.sling.utils.depgen.DepGenConstants;
import org.konzult.sling.utils.depgen.api.handler.POMGenerator;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SlingServlet(
	methods = {HttpConstants.METHOD_POST},
	resourceTypes={DepGenConstants.RT_GENERATOR}
)
@Properties({
	@Property(name = Constants.SERVICE_PID, value = "org.konzult.sling.utils.depgen.impl.SlingDependencyGeneratorServlet"),
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Handles requests for dependency pom"),
	@Property(name = Constants.SERVICE_VENDOR, value = DepGenConstants.VENDOR),
	@Property(name = Constants.VISIBILITY_PRIVATE, value = "true")
})
public class GeneratePostServlet extends SlingAllMethodsServlet {
	
	private static final long serialVersionUID = -4431177184008596134L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePostServlet.class);
	
	private static final String PN_UPLOAD_FIELD = "uploadfield";
	
	@Reference
	private POMGenerator pomGenerator;
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, 
				IOException {
		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				final RequestParameter fileParam = request.getRequestParameterMap().getValue(PN_UPLOAD_FIELD);
				
				final DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
	            builder.setValidating(Boolean.TRUE);
	            builder.setIgnoringElementContentWhitespace(Boolean.TRUE);
	            final Document template = builder.newDocumentBuilder().parse(fileParam.getInputStream());
	            Node importedNode = template.importNode(pomGenerator.generate(Boolean.TRUE), Boolean.TRUE);
	            Node project = template.getElementsByTagName("project").item(0);
	            project.appendChild(importedNode);
	            
	            response.setContentType("application/xml");
	            response.setHeader("Content-Disposition","attachment;" + fileParam.getFileName());
	            
	            prettyPrint(template, response.getWriter(), 2);
			} catch (Exception e) {
				LOGGER.warn("Unable to parse the template!");
				throw new ServletException(e);
			}
		}
	}
	
	private void prettyPrint(Document document, Writer outputStream, int indent) 
					throws TransformerFactoryConfigurationError, TransformerException {
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    if (indent > 0) {
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
	    }
	    Result result = new StreamResult(outputStream);
	    Source source = new DOMSource(document);
	    transformer.transform(source, result);
	}
}