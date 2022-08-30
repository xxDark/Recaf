package me.coley.recaf.mixin;

import me.coley.recaf.decompile.DecompileManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;

@Mixin(DecompileManager.class)
public class MixinDecompilerManager {

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		JOptionPane.showMessageDialog(null, "hello world");
	}
}
