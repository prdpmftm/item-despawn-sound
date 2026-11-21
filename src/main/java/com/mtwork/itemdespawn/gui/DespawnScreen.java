package com.mtwork.itemdespawn.gui;

import com.mtwork.itemdespawn.DespawnConfig;
import com.mtwork.itemdespawn.audio.DespawnSounds;
import com.mtwork.itemdespawn.audio.TrackLibrary;
import com.mtwork.itemdespawn.subtitle.SubtitleOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/**
 * Mod Menu 里的配置界面。
 */
public class DespawnScreen extends Screen {
	private static final int W = 210;
	private final Screen parent;
	private String hint = "";
	private long hintUntil = 0L;

	public DespawnScreen(Screen parent) {
		super(Component.literal("物品消失提示"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		// 每次打开界面都重扫一次，用户手动放进文件夹的音频能立刻出现
		TrackLibrary.scan();
		DespawnConfig cfg = DespawnConfig.get();
		if (!cfg.track.isEmpty() && !TrackLibrary.has(cfg.track)) {
			cfg.track = "";
			DespawnConfig.save();
		}
		int cx = this.width / 2;
		boolean sound = cfg.playsSound();
		boolean text = cfg.showsSubtitle();
		int rows = 2;
		if (sound) {
			rows += 4;
		}
		if (text) {
			rows += 2;
		}
		rows += 2;
		if (sound && !text) {
			rows += 1;
		}
		int rowH = 21;
		int totalH = rows * rowH - 1;
		int top = Math.max(26, (this.height - totalH) / 2);
		int x = cx - W / 2;
		int y = top;

		this.addRenderableWidget(CycleButton.onOffBuilder(cfg.enabled)
				.create(x, y, W, 20, Component.literal("启用"),
						(button, value) -> {
							cfg.enabled = value;
							DespawnConfig.save();
						}));
		y += rowH;

		List<String> modes = List.of("sound", "subtitle", "both");
		List<String> modeLabels = List.of("只声音", "只字幕", "声音+字幕");
		this.addRenderableWidget(CycleButton
				.builder((String value) -> Component.literal(modeLabels.get(modes.indexOf(value))))
				.withValues(modes)
				.withInitialValue(cfg.mode)
				.create(x, y, W, 20, Component.literal("提示方式"),
						(button, value) -> {
							cfg.mode = value;
							DespawnConfig.save();
							this.rebuildWidgets();
						}));
		y += rowH;

		if (sound) {
			List<String> options = new ArrayList<>();
			options.add("");
			options.addAll(TrackLibrary.names());
			this.addRenderableWidget(CycleButton
					.builder((String value) -> Component.literal(value.isEmpty() ? "原版音效" : value))
					.withValues(options)
					.withInitialValue(cfg.track)
					.displayOnlyValue()
					.create(x, y, W, 20, Component.literal("音轨"),
							(button, value) -> {
								cfg.track = value;
								DespawnConfig.save();
							}));
			y += rowH;

			this.addRenderableWidget(new ValueSlider(x, y, W, 20, "音量", 0.0D, 1.0D, cfg.volume,
					value -> {
						cfg.volume = value;
						DespawnConfig.save();
					}));
			y += rowH;

			this.addRenderableWidget(new ValueSlider(x, y, W, 20, "音调", 0.5D, 2.0D, cfg.pitch,
					value -> {
						cfg.pitch = value;
						DespawnConfig.save();
					}));
			y += rowH;

			this.addRenderableWidget(Button.builder(Component.literal("试听"), b -> DespawnSounds.preview())
					.bounds(x, y, W / 2 - 2, 20).build());
			this.addRenderableWidget(Button.builder(Component.literal("导入音频…"), b -> this.importAudio())
					.bounds(x + W / 2 + 2, y, W / 2 - 2, 20).build());
			y += rowH;
		}

		if (text) {
			int[] presets = {1500, 3000, 5000, 8000};
			List<String> presetLabels = List.of("1.5 秒", "3 秒", "5 秒", "8 秒");
			int preset = 1;
			for (int i = 0; i < presets.length; i++) {
				if (Math.abs(presets[i] - cfg.subtitleMs) < Math.abs(presets[preset] - cfg.subtitleMs)) {
					preset = i;
				}
			}
			this.addRenderableWidget(CycleButton
					.builder((String value) -> Component.literal(value))
					.withValues(presetLabels)
					.withInitialValue(presetLabels.get(preset))
					.create(x, y, W, 20, Component.literal("字幕停留"),
							(button, value) -> {
								cfg.subtitleMs = presets[presetLabels.indexOf(value)];
								DespawnConfig.save();
							}));
			y += rowH;

			if (sound) {
				this.addRenderableWidget(Button.builder(Component.literal("预览字幕"), b -> SubtitleOverlay.preview())
						.bounds(x, y, W / 2 - 2, 20).build());
				this.addRenderableWidget(Button.builder(Component.literal("打开文件夹"), b -> this.openFolder())
						.bounds(x + W / 2 + 2, y, W / 2 - 2, 20).build());
			} else {
				this.addRenderableWidget(Button.builder(Component.literal("预览字幕"), b -> SubtitleOverlay.preview())
						.bounds(x, y, W, 20).build());
			}
			y += rowH;
		} else if (sound) {
			this.addRenderableWidget(Button.builder(Component.literal("打开音频文件夹"), b -> this.openFolder())
					.bounds(x, y, W, 20).build());
			y += rowH;
		}

		this.addRenderableWidget(CycleButton.onOffBuilder(cfg.lenient)
				.create(x, y, W, 20, Component.literal("宽松判定"),
						(button, value) -> {
							cfg.lenient = value;
							DespawnConfig.save();
						}));
		y += rowH;

		this.addRenderableWidget(Button.builder(Component.literal("完成"), b -> this.onClose())
				.bounds(x, y, W, 20).build());
	}

	private void openFolder() {
		try {
			Util.getPlatform().openFile(TrackLibrary.folder().toFile());
		} catch (Throwable t) {
			this.setHint("无法打开文件夹：" + TrackLibrary.folder());
		}
	}

	private void importAudio() {
		String picked = FileDialogs.pickOgg(TrackLibrary.folder().toAbsolutePath().toString());
		if (picked == null) {
			this.setHint("已取消");
			return;
		}
		this.importPaths(List.of(picked.split("\\|")));
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		this.importPaths(paths.stream().map(Path::toString).toList());
	}

	private void importPaths(List<String> paths) {
		int ok = 0;
		String last = null;
		for (String path : paths) {
			if (path == null || path.isBlank()) {
				continue;
			}
			String name = TrackLibrary.importFile(Path.of(path.trim()));
			if (name != null) {
				ok++;
				last = name;
			}
		}
		if (ok > 0) {
			DespawnConfig.get().track = last;
			DespawnConfig.save();
			this.rebuildWidgets();
			this.setHint("已导入 " + ok + " 个音频，当前音轨：" + last);
		} else {
			this.setHint("导入失败：只支持 .ogg / .mp3 文件");
		}
	}

	private void setHint(String text) {
		this.hint = text;
		this.hintUntil = System.currentTimeMillis() + 6000L;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
		DespawnConfig cfg = DespawnConfig.get();
		String tip;
		if (cfg.playsSound() && cfg.showsSubtitle()) {
			tip = "字幕在快捷栏上方 · 音频在 config/itemdespawn/sounds";
		} else if (cfg.playsSound()) {
			tip = "音频文件夹：config/itemdespawn/sounds（.ogg / .mp3）";
		} else {
			tip = "物品消失时在快捷栏上方显示文字，不播放声音";
		}
		graphics.drawCenteredString(this.font, Component.literal(tip), this.width / 2, this.height - 34, 0xA0A0A0);
		if (!this.hint.isEmpty() && System.currentTimeMillis() < this.hintUntil) {
			graphics.drawCenteredString(this.font, Component.literal(this.hint), this.width / 2, this.height - 20,
					0x55FF55);
		}
	}

	@Override
	public void onClose() {
		DespawnConfig.save();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static final class ValueSlider extends AbstractSliderButton {
		private final String label;
		private final double min;
		private final double max;
		private final DoubleConsumer onChanged;

		private ValueSlider(int x, int y, int width, int height, String label, double min, double max, double value,
				DoubleConsumer onChanged) {
			super(x, y, width, height, Component.empty(), (value - min) / (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.onChanged = onChanged;
			this.updateMessage();
		}

		private double actual() {
			return this.min + (this.max - this.min) * this.value;
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.literal(String.format("%s：%.2f", this.label, this.actual())));
		}

		@Override
		protected void applyValue() {
			this.onChanged.accept(this.actual());
		}
	}
}
