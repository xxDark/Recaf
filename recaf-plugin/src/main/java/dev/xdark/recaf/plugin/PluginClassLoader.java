package dev.xdark.recaf.plugin;

import me.coley.recaf.classloading.ClassLoaderGroup;
import me.coley.recaf.classloading.EnhancedClassLoader;

import java.net.URL;

/**
 * {@link ClassLoader} designed specifically for plugins.
 *
 * @author xDark
 */
public final class PluginClassLoader extends EnhancedClassLoader {
	private final ClassLoaderGroup<PluginClassLoader> group;

	/**
	 * @param group
	 * 		Class loader group.
	 * @param urls
	 *        {@link URL[]} array used for classpath.
	 * @param parent
	 * 		Parent {@link ClassLoader}.
	 */
	public PluginClassLoader(ClassLoaderGroup<PluginClassLoader> group, URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.group = group;
	}

	@Override
	protected Class<?> altFindClass(String name) throws ClassNotFoundException {
		// Try parent class loader first.
		try {
			return parent.loadClass(name);
		} catch (ClassNotFoundException ignored) {
		}

		// Now, try all other loaders.
		for (PluginClassLoader loader : group) {
			if (loader == this) {
				continue;
			}
			try {
				return loader.lookupClass(name);
			} catch (ClassNotFoundException ignored) {
			}
		}

		throw new ClassNotFoundException(name);
	}

	/**
	 * Registers this loader in a set of
	 * existing loaders.
	 */
	public void register() {
		group.add(this);
	}

	static {
		ClassLoader.registerAsParallelCapable();
	}
}
