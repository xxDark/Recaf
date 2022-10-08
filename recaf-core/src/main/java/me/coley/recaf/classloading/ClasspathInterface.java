package me.coley.recaf.classloading;

import java.io.InputStream;
import java.net.URL;

public interface ClasspathInterface {
	URL[] getClassPath();

	InputStream getResourceAsStream(String path);

	URL getResource(String path);
}
