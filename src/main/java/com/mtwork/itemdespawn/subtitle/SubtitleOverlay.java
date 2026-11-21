package com.mtwork.itemdespawn.subtitle;

import com.mtwork.itemdespawn.DespawnConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕底部的字幕条：物品消失时把提示画在快捷栏上方，给没有外放设备的人看。
 * 同一件物品在显示期间再次消失会合并成计数，最多同时显示三行。
 */
public final class SubtitleOverlay {
	private static final int MAX_LINES = 3;
	private static final long FADE_MS = 700L;
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private record Entry(String name, int count, long expireAt) {
	}

	private SubtitleOverlay() {
	}

	/** 物品实体消失时调用。 */
	public static void push(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		String name = stack.getHoverName().getString().replaceAll("§.", "");
		if (name.isBlank()) {
			name = "物品";
		}
		if (name.length() > 40) {
			name = name.substring(0, 40) + "…";
		}
		push(name, stack.getCount());
	}

	public static synchronized void push(String name, int count) {
		long now = System.currentTimeMillis();
		long life = Math.max(500L, DespawnConfig.get().subtitleMs);
		for (int i = 0; i < ENTRIES.size(); i++) {
			Entry entry = ENTRIES.get(i);
			if (entry.expireAt > now && entry.name.equals(name)) {
				ENTRIES.set(i, new Entry(name, entry.count + Math.max(1, count), now + life));
				return;
			}
		}
		ENTRIES.add(new Entry(name, Math.max(1, count), now + life));
		while (ENTRIES.size() > MAX_LINES) {
			ENTRIES.remove(0);
		}
	}

	/** 配置界面的“预览字幕”按钮用。 */
	public static void preview() {
		push("钻石", 3);
	}

	/** 在 HUD 上画字幕，由 GuiMixin 调起。 */
	public static void render(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.font == null) {
			return;
		}
		long now = System.currentTimeMillis();
		List<Entry> snapshot;
		synchronized (SubtitleOverlay.class) {
			ENTRIES.removeIf(entry -> entry.expireAt <= now);
			if (ENTRIES.isEmpty()) {
				return;
			}
			snapshot = new ArrayList<>(ENTRIES);
		}
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int lineHeight = 11;
		int bottom = height - 50;
		int count = snapshot.size();
		for (int i = 0; i < count; i++) {
			Entry entry = snapshot.get(i);
			int y = bottom - (count - 1 - i) * (lineHeight + 3);
			Component text = Component.literal(entry.count > 1
					? "物品消失：" + entry.name + " ×" + entry.count
					: "物品消失：" + entry.name);
			int textWidth = minecraft.font.width(text);
			int x = (width - textWidth) / 2;
			long left = entry.expireAt - now;
			int alpha = left >= FADE_MS ? 255 : (int) Math.max(0L, left * 255L / FADE_MS);
			if (alpha <= 4) {
				continue;
			}
			int background = (alpha * 0x66 / 255) << 24;
			graphics.fill(x - 4, y - 2, x + textWidth + 4, y + lineHeight, background);
			graphics.drawString(minecraft.font, text, x, y, (alpha << 24) | 0xFFFFFF, true);
		}
	}
}
