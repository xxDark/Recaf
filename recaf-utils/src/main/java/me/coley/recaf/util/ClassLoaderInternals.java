package me.coley.recaf.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Hacky code to get internal classloader state.
 *
 * @author xDark - This was his idea.
 * @author Matt Coley - I actually committed the file.
 */
public class ClassLoaderInternals {
	/**
	 * @return {@link jdk.internal.loader.URLClassPath} instance.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the internals change and the reflective look-ups fail.
	 */
	public static Object getUcp() throws ReflectiveOperationException {
		// Fetch UCP of application's ClassLoader
		// - ((ClassLoaders.AppClassLoader) ClassLoaders.appClassLoader()).ucp
		ClassLoader appClassLoader = ClassLoaderInternals.class.getClassLoader();
		Class<?> ucpOwner = appClassLoader.getClass();
		Field ucpField = null;
		do {
			try {
				ucpField = ReflectUtil.getDeclaredField(ucpOwner, "ucp");
			} catch (NoSuchFieldException ignored) {
				ucpOwner = ucpOwner.getSuperclass();
			}
		} while (ucpField == null && ucpOwner != null);
		if (ucpField == null) {
			throw new RuntimeException("JDK internals changed");
		}
		return ucpField.get(appClassLoader);
	}

	/**
	 * @param ucp
	 * 		See {@link #getUcp()}.
	 *
	 * @return The contents of the UCP.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the internals change and the reflective look-ups fail.
	 */
	@SuppressWarnings("unchecked")
	public static List<URL> getUcpPathList(Object ucp) throws ReflectiveOperationException {
		if (ucp == null)
			return Collections.emptyList();
		Class<?> ucpClass = ucp.getClass();
		Field path = ReflectUtil.getDeclaredField(ucpClass, "path");
		return (List<URL>) path.get(ucp);
	}

	/**
	 * @param ucp
	 * 		See {@link #getUcp()}.
	 * @param url
	 * 		URL to add to the search path for directories and Jar files of the UCP.
	 *
	 * @throws ReflectiveOperationException
	 * 		When the internals change and the reflective look-ups fail.
	 */
	public static void appendToUcpPath(Object ucp, URL url) throws ReflectiveOperationException {
		if (ucp == null)
			return;
		Class<?> ucpClass = ucp.getClass();
		Method addURL = ReflectUtil.getDeclaredMethod(ucpClass, "addURL", URL.class);
		addURL.invoke(ucp, url);
	}
}
