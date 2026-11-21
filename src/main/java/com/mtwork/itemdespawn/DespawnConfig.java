package com.mtwork.itemdespawn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 模组配置，保存在 config/itemdespawn/config.json。
 */
public final class DespawnConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("itemdespawn/config.json");

	private static DespawnConfig instance = new DespawnConfig();

	/** 总开关。 */
	public boolean enabled = true;
	/** 选中的音轨文件名（空字符串 = 使用原版音效）。 */
	public String track = "";
	/** 音量 0.0 - 1.0。 */
	public double volume = 1.0D;
	/** 音调 0.5 - 2.0。 */
	public double pitch = 1.0D;
	/** 判定为“到期消失”的最小存在时长（tick），原版 6000。 */
	public int minAgeTicks = 5980;
	/** 宽松模式：不检查物品年龄，任何掉落物消失都播放（多人服务器上更保险，但可能误响）。 */
	public boolean lenient = false;
	/** 两声之间的最短间隔（毫秒），避免一叠物品同时消失时炸麦。 */
	public int cooldownMs = 250;
	/** 提示方式：sound = 只播放声音，subtitle = 只显示字幕，both = 两者都要。 */
	public String mode = "both";
	/** 字幕停留时间（毫秒）。 */
	public int subtitleMs = 3000;

	public static DespawnConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.isRegularFile(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE)) {
				DespawnConfig loaded = GSON.fromJson(reader, DespawnConfig.class);
				if (loaded != null) {
					instance = loaded.sanitize();
				}
			} catch (Exception e) {
				ItemDespawnClient.LOGGER.warn("读取配置失败，使用默认配置", e);
			}
		}
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			ItemDespawnClient.LOGGER.warn("保存配置失败", e);
		}
	}

	private DespawnConfig sanitize() {
		if (volume < 0.0D) volume = 0.0D;
		if (volume > 1.0D) volume = 1.0D;
		if (pitch < 0.5D) pitch = 0.5D;
		if (pitch > 2.0D) pitch = 2.0D;
		if (minAgeTicks < 0) minAgeTicks = 0;
		if (cooldownMs < 0) cooldownMs = 0;
		if (track == null) track = "";
		if (!"sound".equals(mode) && !"subtitle".equals(mode) && !"both".equals(mode)) {
			mode = "both";
		}
		if (subtitleMs < 800) subtitleMs = 800;
		if (subtitleMs > 20000) subtitleMs = 20000;
		return this;
	}

	/** 是否需要播放声音。 */
	public boolean playsSound() {
		return "sound".equals(mode) || "both".equals(mode);
	}

	/** 是否需要显示字幕。 */
	public boolean showsSubtitle() {
		return "subtitle".equals(mode) || "both".equals(mode);
	}
}
