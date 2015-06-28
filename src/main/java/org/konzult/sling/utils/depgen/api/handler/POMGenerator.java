package org.konzult.sling.utils.depgen.api.handler;

import java.io.IOException;

import org.w3c.dom.Node;

public interface POMGenerator {
	
	Node generate(boolean includeVersionScope) throws IOException;
}
