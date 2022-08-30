package dev.xdark.recaf.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class MixinServiceRecafBootstrap implements IMixinServiceBootstrap {

	@Override
	public String getName() {
		return "Recaf";
	}

	@Override
	public String getServiceClassName() {
		return MixinServiceRecaf.class.getName();
	}

	@Override
	public void bootstrap() {
	}
}
