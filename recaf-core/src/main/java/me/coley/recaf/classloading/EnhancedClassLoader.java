package me.coley.recaf.classloading;

import me.coley.recaf.io.ByteSource;
import me.coley.recaf.io.ByteSources;
import me.coley.recaf.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Manifest;

public class EnhancedClassLoader extends URLClassLoader implements ClassLoaderInterface {
	protected final ClassLoader parent;
	protected ClassLoaderInterface cli;

	public EnhancedClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, null);
		this.parent = parent;
		cli = new AbstractClassLoaderInterface() {
			@Override
			public URL[] getClassPath() {
				throw new UnsupportedOperationException("Should never be called");
			}

			@Override
			public InputStream getResourceAsStream(String path) {
				throw new UnsupportedOperationException("Should never be called");
			}

			@Override
			public URL getResource(String path) {
				throw new UnsupportedOperationException("Should never be called");
			}

			@Override
			public Class<?> defineNewClass(String className, byte[] bytes, int off, int len) {
				throw new UnsupportedOperationException("Should never be called");
			}
		};
	}

	public EnhancedClassLoader(URL[] urls) {
		this(urls, EnhancedClassLoader.class.getClassLoader());
	}

	@Override
	public ClassDefinition applyTransformers(ClassDefinition definition) throws IOException {
		return cli.applyTransformers(definition);
	}

	@Override
	public URL[] getClassPath() {
		return getURLs();
	}

	@Override
	public void registerTransformer(ClassFileTransformer transformer) {
		cli.registerTransformer(transformer);
	}

	@Override
	public void removeTransformer(ClassFileTransformer transformer) {
		cli.removeTransformer(transformer);
	}

	@Override
	public void addTransformerExclusion(Predicate<String> exclusion) {
		cli.addTransformerExclusion(exclusion);
	}

	@Override
	public void addTransformerExclusion(Collection<String> exclusions) {
		cli.addTransformerExclusion(exclusions);
	}

	@Override
	public void addLoadingExclusion(Predicate<String> exclusion) {
		cli.addLoadingExclusion(exclusion);
	}

	@Override
	public void addLoadingExclusion(Collection<String> exclusions) {
		cli.addLoadingExclusion(exclusions);
	}

	@Override
	public boolean isTransformationExcluded(String className) {
		return cli.isTransformationExcluded(className);
	}

	@Override
	public boolean isLoadingExcluded(String className) {
		return cli.isLoadingExcluded(className);
	}

	@Override
	public Class<?> defineNewClass(String className, byte[] bytes, int off, int len) {
		return defineClass(className, bytes, off, len);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ClassLoaderInterface cli = this.cli;
		load:
		if (!cli.isLoadingExcluded(name)) {
			// We prioritize our classes over others.
			if (cli.isTransformationExcluded(name)) {
				try {
					return super.findClass(name);
				} catch (ClassNotFoundException ignored) {
					break load;
				}
			}
			String path = name.replace('.', '/').concat(".class");
			URL url = findResource(path);
			if (url != null) {
				int lastDot = name.lastIndexOf('.');
				CodeSource cs = null;
				try {
					URLConnection connection = url.openConnection();
					connection.setUseCaches(false);
					connection.connect();
					if (connection instanceof JarURLConnection) {
						JarURLConnection juc = (JarURLConnection) connection;
						Certificate[] certificates = juc.getCertificates();
						url = juc.getJarFileURL(); // Force url to base url
						cs = new CodeSource(url, certificates);
						if (lastDot != -1) {
							String packageName = name.substring(0, lastDot);
							if (getDefinedPackage(packageName) == null) {
								Manifest manifest = juc.getManifest();
								URL codeSourceURL = juc.getJarFileURL();
								if (manifest != null) {
									definePackage(packageName, manifest, codeSourceURL);
								} else {
									definePackage(packageName, null, null, null, null, null, null, null);
								}
							}
						}
					}
					// TODO optimization
					ByteSource source;
					try (InputStream in = connection.getInputStream()) {
						byte[] bc = IOUtil.toByteArray(in);
						source = ByteSources.wrap(bc);
					}
					ClassDefinition definition = new ClassDefinition(source);
					ClassDefinition newDefinition = cli.applyTransformers(definition);
					if (cs == null || (newDefinition != definition && cs.getCertificates() != null)) {
						cs = new CodeSource(url, (Certificate[]) null); // Erase certificates
					}
					byte[] bytes = newDefinition.getBytecode().readAll();
					return defineClass(null, bytes, 0, bytes.length, cs);
				} catch (IOException ex) {
					throw new ClassNotFoundException(name, ex);
				}
			}
		}
		return altFindClass(name);
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			cli = null;
		}
	}

	/**
	 * Helper method to locate a class directly
	 * in this loader.
	 *
	 * @param name
	 * 		the name of the class.
	 *
	 * @return the resulting class.
	 *
	 * @throws ClassNotFoundException
	 * 		if the class could not be found,
	 * 		or if the loader is closed.
	 */
	public final Class<?> lookupClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}

	protected Class<?> altFindClass(String name) throws ClassNotFoundException {
		return parent.loadClass(name);
	}

	// Mirrored from URLClassPath
	public static URL[] parse(String cp) {
		List<URL> path = new ArrayList<>();
		if (cp != null) {
			int off = 0, next;
			do {
				next = cp.indexOf(File.pathSeparator, off);
				String element = (next == -1)
						? cp.substring(off)
						: cp.substring(off, next);
				try {
					URL url = new File(element).getCanonicalFile().toURI().toURL();
					path.add(url);
				} catch (IOException ignored) {
				}
				off = next + 1;
			} while (next != -1);
		}
		return path.toArray(new URL[0]);
	}

	static {
		ClassLoader.registerAsParallelCapable();
	}
}
