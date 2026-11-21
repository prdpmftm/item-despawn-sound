package com.mtwork.itemdespawn.mixin;

import com.mtwork.itemdespawn.subtitle.SubtitleOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 HUD 画完之后补一层字幕。
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void itemdespawn$renderSubtitles(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		SubtitleOverlay.render(graphics);
	}
}
