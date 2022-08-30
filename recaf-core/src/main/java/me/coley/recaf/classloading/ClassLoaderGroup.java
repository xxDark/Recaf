package me.coley.recaf.classloading;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Group of class loaders.
 *
 * @author xDark
 */
public final class ClassLoaderGroup<T extends ClassLoader> implements Iterable<T> {
	private final Set<T> loaders = ConcurrentHashMap.newKeySet();

	public void add(T loader) {
		loaders.add(loader);
	}

	public boolean has(T loader) {
		return loaders.contains(loader);
	}

	public boolean remove(T loader) {
		return loaders.remove(loader);
	}

	@Override
	public Iterator<T> iterator() {
		return loaders.iterator();
	}
}
