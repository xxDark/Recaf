package dev.xdark.recaf.mixin;

import me.coley.recaf.classloading.ClassDefinition;
import me.coley.recaf.classloading.ClasspathInterface;
import me.coley.recaf.classloading.TransformerInterface;
import me.coley.recaf.io.ByteSources;
import me.coley.recaf.util.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.MainAttributes;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.Files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MixinServiceRecaf extends MixinServiceAbstract implements
		IClassProvider, IClassBytecodeProvider,
		ITransformerProvider {
	private final TransformerInterface transformerInterface;
	private final ClasspathInterface classpathInterface;

	public MixinServiceRecaf() {
		MixinThreadState state = MixinThreadState.get();
		transformerInterface = state.getTransformerInterface();
		classpathInterface = state.getClasspathInterface();
	}

	@Override
	public String getName() {
		return "Recaf";
	}

	@Override
	public boolean isValid() {
		try {
			Class.forName("me.coley.recaf.Recaf", false, MixinServiceRecaf.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException ignored) {
			return false;
		}
	}

	@Override
	public void offer(IMixinInternal internal) {
		if (internal instanceof IMixinTransformerFactory) {
			IMixinTransformer transformer = ((IMixinTransformerFactory) internal).createTransformer();
			transformerInterface.registerTransformer(definition -> {
				ClassReader reader = definition.getReader();
				String name = reader.getClassName().replace('/', '.');
				byte[] bc = definition.getRawBytecode();
				byte[] transformed = transformer.transformClassBytes(name, name, bc);
				if (transformed == bc) {
					return definition;
				}
				return new ClassDefinition(ByteSources.wrap(transformed));
			});
		}
		super.offer(internal);
	}

	@Override
	public IClassProvider getClassProvider() {
		return this;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return this;
	}

	@Override
	public ITransformerProvider getTransformerProvider() {
		return this;
	}

	@Override
	public IClassTracker getClassTracker() {
		return null;
	}

	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}

	@Override
	public Collection<String> getPlatformAgents() {
		return List.of();
	}

	@Override
	public IContainerHandle getPrimaryContainer() {
		try {
			URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			return new ContainerHandleURI(uri);
		} catch (URISyntaxException ignored) {
		}
		return new ContainerHandleVirtual(getName());
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return classpathInterface.getResourceAsStream(name);
	}

	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
		return getClassNode(name, false);
	}

	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		URL url = classpathInterface.getResource(name.replace('.', '/').concat(".class"));
		if (url == null) {
			throw new ClassNotFoundException(name);
		}
		ClassReader reader;
		try (InputStream in = url.openStream()) {
			if (runTransformers) {
				ClassDefinition definition = new ClassDefinition(ByteSources.wrap(IOUtil.toByteArray(in)));
				definition = transformerInterface.applyTransformers(definition);
				reader = definition.getReader();
			} else {
				reader = new ClassReader(in);
			}
		}
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		return node;
	}

	@Override
	public URL[] getClassPath() {
		return classpathInterface.getClassPath();
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name, false);
	}

	@Override
	public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
		if (name.startsWith("org.spongepowered.")) {
			throw new ClassNotFoundException(name);
		}
		Class<?> c = Class.forName(name, initialize, MixinServiceRecaf.class.getClassLoader());
		if (c == Mixin.class) {
			throw new ClassNotFoundException(name);
		}
		if (c.getDeclaredAnnotation(Mixin.class) != null) {
			throw new ClassNotFoundException(name);
		}
		return c;
	}

	@Override
	public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, MixinServiceRecaf.class.getClassLoader());
	}

	@Override
	public Collection<ITransformer> getTransformers() {
		return List.of();
	}

	@Override
	public Collection<ITransformer> getDelegatedTransformers() {
		return List.of();
	}

	@Override
	public void addTransformerExclusion(String name) {
		transformerInterface.addTransformerExclusion(name);
	}

	@Override
	public Collection<IContainerHandle> getMixinContainers() {
		List<IContainerHandle> containers = new ArrayList<>();
		for (URL url : classpathInterface.getClassPath()) {
			URI uri;
			try {
				uri = url.toURI();
			} catch (URISyntaxException ex) {
				throw new IllegalStateException(ex);
			}
			if (!"file".equals(uri.getScheme()) || !Files.toFile(uri).exists()) {
				continue;
			}
			MainAttributes attributes = MainAttributes.of(uri);
			if (attributes.get("MixinConfigs") != null) {
				containers.add(new ContainerHandleURI(uri));
			}
		}
		return containers;
	}
}
