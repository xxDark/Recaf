package dev.xdark.recaf.mixin;

import me.coley.recaf.classloading.ClassLoaderInterface;
import org.spongepowered.asm.launch.MixinBridge;
import org.spongepowered.asm.launch.platform.CommandLineOptions;

import java.util.List;
import java.util.Properties;

/**
 * Mixin bootstrap helper.
 *
 * @author xDark
 */
public final class MixinBootstrap {
	private static final String BOOTSTRAP_PROPERTY = "mixin.bootstrapService";
	private static final String SERVICE_PROPERTY = "mixin.service";

	private MixinBootstrap() {
	}

	public static void bootstrap(List<String> args) {
		ClassLoaderInterface cli = (ClassLoaderInterface) MixinBootstrap.class.getClassLoader();
		MixinThreadState.set(new MixinThreadState(cli));
		Properties properties = System.getProperties();
		synchronized (properties) {
			properties.put(BOOTSTRAP_PROPERTY, MixinServiceRecafBootstrap.class.getName());
			properties.put(SERVICE_PROPERTY, MixinServiceRecaf.class.getName());
			try {
				MixinBridge.start();
				MixinBridge.doInit(CommandLineOptions.of(args));
				MixinBridge.inject();
				MixinBridge.initEnvironment();
			} finally {
				properties.remove(BOOTSTRAP_PROPERTY);
				properties.remove(SERVICE_PROPERTY);
				MixinThreadState.remove();
			}
		}
	}
}
