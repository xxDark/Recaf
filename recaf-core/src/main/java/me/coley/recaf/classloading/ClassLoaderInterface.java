package me.coley.recaf.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface ClassLoaderInterface {

	URL[] getClassPath();

	InputStream getResourceAsStream(String path);

	URL getResource(String path);

	ClassDefinition applyTransformers(ClassDefinition definition) throws IOException;

	void registerTransformer(ClassFileTransformer transformer);

	void removeTransformer(ClassFileTransformer transformer);

	void addTransformerExclusion(Predicate<String> exclusion);

	void addTransformerExclusion(Collection<String> exclusions);

	default void addTransformerExclusion(String... exclusions) {
		addTransformerExclusion(List.of(exclusions));
	}

	void addLoadingExclusion(Predicate<String> exclusion);

	void addLoadingExclusion(Collection<String> exclusions);

	default void addLoadingExclusion(String... exclusions) {
		addLoadingExclusion(List.of(exclusions));
	}

	boolean isTransformationExcluded(String className);

	boolean isLoadingExcluded(String className);

	Class<?> defineNewClass(String className, byte[] bytes, int off, int len);
}
