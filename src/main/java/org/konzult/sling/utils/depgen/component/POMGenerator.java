package org.konzult.sling.utils.depgen.component;

import java.io.IOException;

public interface POMGenerator {
	String generate(boolean includeVersionScope) throws IOException;
}
