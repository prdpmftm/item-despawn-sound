package com.mtwork.itemdespawn.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import com.mtwork.itemdespawn.DespawnConfig;
import com.mtwork.itemdespawn.gui.DespawnScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 两个可自定义按键：打开设置、开关模组。
 * 在「选项 → 控制 → 按键绑定」的「物品消失提示」分类里改键，默认不绑定。
 */
public final class Keybinds {
	private static final String CATEGORY = "key.category.itemdespawn";

	private static KeyMapping openSettings;
	private static KeyMapping toggleMod;

	private Keybinds() {
	}

	public static void init() {
		if (openSettings != null) {
			return;
		}
		openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.itemdespawn.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
		toggleMod = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.itemdespawn.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettings.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new DespawnScreen(null));
				}
			}
			while (toggleMod.consumeClick()) {
				DespawnConfig cfg = DespawnConfig.get();
				cfg.enabled = !cfg.enabled;
				DespawnConfig.save();
				if (client.gui != null) {
					client.gui.setOverlayMessage(Component.literal(
							"物品消失提示：" + (cfg.enabled ? "已开启" : "已关闭")), false);
				}
			}
		});
	}
}
