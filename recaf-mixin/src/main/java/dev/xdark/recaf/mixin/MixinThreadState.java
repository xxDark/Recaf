package dev.xdark.recaf.mixin;

import me.coley.recaf.classloading.ClassLoaderInterface;

/**
 * Mixin thread state.
 *
 * @author xDark
 */
public final class MixinThreadState {
	private static final ThreadLocal<MixinThreadState> TLC = new ThreadLocal<>();

	private final ClassLoaderInterface classLoaderInterface;

	/**
	 * @param classLoaderInterface
	 * 		Class loader interface.
	 */
	public MixinThreadState(ClassLoaderInterface classLoaderInterface) {
		this.classLoaderInterface = classLoaderInterface;
	}

	public ClassLoaderInterface getClassLoaderInterface() {
		return classLoaderInterface;
	}

	public static void set(MixinThreadState state) {
		TLC.set(state);
	}

	public static MixinThreadState get() {
		return TLC.get();
	}

	public static void remove() {
		TLC.remove();
	}
}
