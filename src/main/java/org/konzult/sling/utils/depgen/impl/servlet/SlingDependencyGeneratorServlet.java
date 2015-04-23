package org.konzult.sling.utils.depgen.impl.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.konzult.sling.utils.depgen.DepGenConstants;
import org.konzult.sling.utils.depgen.api.handler.POMGenerator;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(
	methods = {HttpConstants.METHOD_GET},
	paths = {"/bin/generate"},
	extensions = {"html"}
)
@Properties({
	@Property(name = Constants.SERVICE_PID, value = "org.konzult.sling.utils.depgen.impl.SlingDependencyGeneratorServlet"),
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Handles requests for dependency pom"),
	@Property(name = Constants.SERVICE_VENDOR, value = DepGenConstants.VENDOR),
	@Property(name = Constants.VISIBILITY_PRIVATE, value = "true")
})
public class SlingDependencyGeneratorServlet extends SlingSafeMethodsServlet {
	
	private static final long serialVersionUID = -4431177184008596134L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SlingDependencyGeneratorServlet.class);
	
	@Reference
	private POMGenerator pomGenerator;
	
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		
		final long start = Calendar.getInstance().getTimeInMillis();
		String gen = pomGenerator.generate(true);
		PrintWriter outWriter = response.getWriter();
		
		response.setContentType("text/xml");
		outWriter.print(gen);
		
		LOGGER.info("Measured time: {}", Calendar.getInstance().getTimeInMillis() - start);
		
		outWriter.flush();
	}
}