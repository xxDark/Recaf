package me.coley.recaf.classloading;

import java.io.IOException;

/**
 * Class file transformer.
 *
 * @author xDark
 */
@FunctionalInterface
public interface ClassFileTransformer {

	/**
	 * Transforms class definition.
	 *
	 * @param definition
	 * 		Class definition to transform.
	 *
	 * @return New class definition or {@code definition}, if no changes.
	 */
	ClassDefinition transform(ClassDefinition definition) throws IOException;
}
