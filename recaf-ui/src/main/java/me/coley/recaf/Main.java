package me.coley.recaf;

import me.coley.recaf.classloading.EnhancedClassLoader;
import me.coley.recaf.classloading.TransformerInterface;

import java.lang.reflect.Method;

/**
 * Entry point.
 *
 * @author Matt Coley
 */
public class Main {

	/**
	 * Main entry point.
	 *
	 * @param args
	 * 		Program arguments.
	 */
	public static void main(String[] args) throws ReflectiveOperationException {
		// Another solution is to set "java.system.class.loader" at startup,
		// but we can't to do that.
		if (new Exception().getStackTrace().length != 1) {
			System.err.println("Recaf was launched from another location");
		}
		ClassLoader cl = Main.class.getClassLoader();
		String cp = System.getProperty("java.class.path");
		EnhancedClassLoader classLoader = new EnhancedClassLoader(EnhancedClassLoader.parse(cp), cl);
		TransformerInterface ti = classLoader.getTransformerInterface();
		ti.addLoadingExclusion("org.objectweb.asm.");
		ti.addLoadingExclusion("me.coley.recaf.io.");
		ti.addLoadingExclusion("me.coley.recaf.classloading.");
		ti.addTransformerExclusion("org.spongepowered.");
		Method m = Class.forName("me.coley.recaf.RecafMain", true, classLoader)
				.getDeclaredMethod("main", String[].class);
		m.setAccessible(true);
		m.invoke(null, new Object[]{args});
	}
}
