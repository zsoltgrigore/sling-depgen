package org.konzult.sling.utils.depgen.api.handler;

import java.io.IOException;

public interface POMGenerator {
	String generate(boolean includeVersionScope) throws IOException;
}
