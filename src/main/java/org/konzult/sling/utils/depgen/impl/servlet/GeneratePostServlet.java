package org.konzult.sling.utils.depgen.impl.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.konzult.sling.utils.depgen.DepGenConstants;
import org.konzult.sling.utils.depgen.api.handler.POMGenerator;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	
	@Reference
	private POMGenerator pomGenerator;
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, 
				IOException {
		if (ServletFileUpload.isMultipartContent(request)) {
			
		}
		
		try {
	        List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (FileItem item : items) {
	            if (!item.isFormField()) {
	                // Process form file field (input type="file").
	                if(item.getFieldName() == "fileupload") {
		                String fileName = FilenameUtils.getName(item.getName());
		                final DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
		                builder.setValidating(Boolean.TRUE);
		                builder.setIgnoringElementContentWhitespace(Boolean.TRUE);
		                Document template = builder.newDocumentBuilder().parse(item.getInputStream());
		                final NodeList found =template.getElementsByTagName("dependencies");
		                
		                if (found.getLength() == 1) {
		                	Node dependenciesNode = found.item(0);
		                }
		                
		                pomGenerator.generate(false);
	                }
	            }
	        }
	    } catch (Exception e) {
	        throw new ServletException("Cannot parse multipart request.", e);

	    }
	}
}