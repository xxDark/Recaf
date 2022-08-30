package me.coley.recaf.classloading;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractClassLoaderInterface implements ClassLoaderInterface {
	private final List<ClassFileTransformer> transformers = Collections.synchronizedList(new ArrayList<>());
	private final Set<Predicate<String>> transformExclusions = new HashSet<>();
	private final Set<Predicate<String>> loadingExclusions = new HashSet<>();

	@Override
	public ClassDefinition applyTransformers(ClassDefinition definition) throws IOException {
		synchronized (transformers) {
			for (ClassFileTransformer transformer : transformers) {
				definition = transformer.transform(definition);
			}
		}
		return definition;
	}

	@Override
	public void registerTransformer(ClassFileTransformer transformer) {
		transformers.add(transformer);
	}

	@Override
	public void removeTransformer(ClassFileTransformer transformer) {
		transformers.remove(transformer);
	}

	@Override
	public void addTransformerExclusion(Predicate<String> exclusion) {
		transformExclusions.add(exclusion);
	}

	@Override
	public void addTransformerExclusion(Collection<String> exclusions) {
		transformExclusions.add(testSet(new HashSet<>(exclusions)));
	}

	@Override
	public void addLoadingExclusion(Predicate<String> exclusion) {
		loadingExclusions.add(exclusion);
	}

	@Override
	public void addLoadingExclusion(Collection<String> exclusions) {
		loadingExclusions.add(testSet(new HashSet<>(exclusions)));
	}

	@Override
	public boolean isTransformationExcluded(String className) {
		for (Predicate<String> exclusion : transformExclusions) {
			if (exclusion.test(className)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isLoadingExcluded(String className) {
		for (Predicate<String> exclusion : loadingExclusions) {
			if (exclusion.test(className)) {
				return true;
			}
		}
		return false;
	}

	private static Predicate<String> testSet(Set<String> set) {
		return className -> {
			for (String exclusion : set) {
				if (className.startsWith(exclusion)) {
					return true;
				}
			}
			return false;
		};
	}
}
