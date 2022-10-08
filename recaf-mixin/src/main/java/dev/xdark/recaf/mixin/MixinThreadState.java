package dev.xdark.recaf.mixin;

import me.coley.recaf.classloading.ClasspathInterface;
import me.coley.recaf.classloading.TransformerInterface;

/**
 * Mixin thread state.
 *
 * @author xDark
 */
public final class MixinThreadState {
	private static final ThreadLocal<MixinThreadState> TLC = new ThreadLocal<>();

	private final TransformerInterface transformerInterface;
	private final ClasspathInterface classpathInterface;

	/**
	 * @param transformerInterface
	 * 		Transformer interface.
	 * @param classpathInterface
	 * 		Classpath interface.
	 */
	public MixinThreadState(TransformerInterface transformerInterface, ClasspathInterface classpathInterface) {
		this.transformerInterface = transformerInterface;
		this.classpathInterface = classpathInterface;
	}

	public TransformerInterface getTransformerInterface() {
		return transformerInterface;
	}

	public ClasspathInterface getClasspathInterface() {
		return classpathInterface;
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
