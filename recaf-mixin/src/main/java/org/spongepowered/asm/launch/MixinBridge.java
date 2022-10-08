package org.spongepowered.asm.launch;

import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Method;

/**
 * Bridge methods for MixinBootstrap.
 *
 * @author xDark
 */
public final class MixinBridge {
	private MixinBridge() {
	}

	public static void start() {
		if (!MixinBootstrap.start()) {
			throw new IllegalStateException("Failed to start mixin environment");
		}
	}

	public static void doInit(CommandLineOptions options) {
		MixinBootstrap.doInit(options);
	}

	public static void init() {
		MixinBootstrap.init();
	}

	public static void inject() {
		MixinBootstrap.inject();
	}

	public static void initEnvironment() {
		try {
			Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
			m.setAccessible(true);
			m.invoke(null, MixinEnvironment.Phase.DEFAULT);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to enter default phase", ex);
		}
	}
}
