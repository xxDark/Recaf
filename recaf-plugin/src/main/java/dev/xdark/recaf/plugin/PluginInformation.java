package dev.xdark.recaf.plugin;

/**
 * Object containing necessary information about a plugin.
 *
 * @author xDark
 */
public final class PluginInformation {
	private final String name;
	private final String version;
	private final String[] authors;
	private final String description;

	/**
	 * @param name
	 * 		name of the plugin.
	 * @param version
	 * 		plugin version.
	 * @param authors
	 * 		authors of the plugin.
	 * @param description
	 * 		plugin description.
	 */
	public PluginInformation(String name, String version, String[] authors, String description) {
		this.name = name;
		this.version = version;
		this.authors = authors;
		this.description = description;
	}

	/**
	 * @return plugin name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return plugin version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return author of the plugin.
	 */
	public String[] getAuthors() {
		return authors;
	}

	/**
	 * @return plugin description.
	 */
	public String getDescription() {
		return description;
	}
}
