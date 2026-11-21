package com.mtwork.itemdespawn.audio;

import com.mtwork.itemdespawn.ItemDespawnClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.ConstantFloat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 扫描 config/itemdespawn/sounds 下的 .ogg 文件，把它们包装成游戏内可播放的 Sound。
 */
public final class TrackLibrary {
	private static final Path SOUNDS_DIR = FabricLoader.getInstance().getConfigDir().resolve("itemdespawn/sounds");

	private static final Map<String, Path> FILES = new LinkedHashMap<>();
	private static final Map<Identifier, Path> BY_ID = new LinkedHashMap<>();
	private static final Map<String, Sound> SOUNDS = new LinkedHashMap<>();

	private TrackLibrary() {
	}

	public static Path folder() {
		return SOUNDS_DIR;
	}

	/** 支持 .ogg（游戏自带解码）与 .mp3（JLayer 解码）。 */
	public static boolean isSupported(Path file) {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".ogg") || name.endsWith(".mp3");
	}

	/** 按扩展名打开对应解码器。 */
	public static AudioStream open(Path file) throws IOException {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
		if (name.endsWith(".mp3")) {
			return new Mp3AudioStream(file.toFile());
		}
		return new JOrbisAudioStream(Files.newInputStream(file));
	}

	/** 把整个音频流读成一块 PCM。 */
	public static ByteBuffer readAll(AudioStream stream) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteBuffer chunk;
		while ((chunk = stream.read(16384)) != null && chunk.hasRemaining()) {
			byte[] bytes = new byte[chunk.remaining()];
			chunk.get(bytes);
			out.write(bytes);
		}
		byte[] data = out.toByteArray();
		// 同样必须是直接缓冲区，否则 alBufferData 会崩溃
		ByteBuffer direct = ByteBuffer.allocateDirect(data.length);
		direct.put(data);
		direct.flip();
		return direct;
	}

	/** 重新扫描音频文件夹。 */
	public static void scan() {
		FILES.clear();
		BY_ID.clear();
		SOUNDS.clear();
		try {
			Files.createDirectories(SOUNDS_DIR);
			try (Stream<Path> stream = Files.walk(SOUNDS_DIR, 1)) {
				stream.filter(Files::isRegularFile)
						.filter(TrackLibrary::isSupported)
						.sorted()
						.forEach(TrackLibrary::register);
			}
		} catch (Exception e) {
			ItemDespawnClient.LOGGER.warn("扫描音频文件夹失败", e);
		}
	}

	private static void register(Path file) {
		String name = file.getFileName().toString();
		Identifier id = Identifier.fromNamespaceAndPath(ItemDespawnClient.MOD_ID, "custom/" + slug(name));
		Sound sound = new Sound(id, ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1,
				Sound.Type.FILE, true, false, 16);
		FILES.put(name, file);
		BY_ID.put(id, file);
		// 播放时游戏用的是 getPath()（sounds/...ogg），两种都登记
		Identifier path = sound.getPath();
		if (path != null) {
			BY_ID.put(path, file);
		}
		SOUNDS.put(name, sound);
	}

	private static String slug(String name) {
		String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
		if (base.length() > 40) {
			base = base.substring(0, 40);
		}
		return base + "_" + Integer.toHexString(name.hashCode());
	}

	/** 可用音轨名（文件名）。 */
	public static List<String> names() {
		return new ArrayList<>(FILES.keySet());
	}

	public static boolean has(String name) {
		return name != null && SOUNDS.containsKey(name);
	}

	public static Sound sound(String name) {
		return SOUNDS.get(name);
	}

	public static Path file(String name) {
		return FILES.get(name);
	}

	/** 供音效流混入使用：Identifier -> 本地 ogg 文件。 */
	public static Path pathFor(Identifier id) {
		return BY_ID.get(id);
	}

	/** 把外部文件复制进音频文件夹，返回落地后的文件名（失败返回 null）。 */
	public static String importFile(Path source) {
		try {
			Files.createDirectories(SOUNDS_DIR);
			String name = source.getFileName().toString();
			String lower = name.toLowerCase(Locale.ROOT);
			if (!lower.endsWith(".ogg") && !lower.endsWith(".mp3")) {
				ItemDespawnClient.LOGGER.warn("不支持的音频格式: {}", name);
				return null;
			}
			Path target = SOUNDS_DIR.resolve(name);
			String ext = lower.endsWith(".mp3") ? ".mp3" : ".ogg";
			String base = name.substring(0, name.length() - 4);
			int counter = 1;
			while (Files.exists(target)) {
				target = SOUNDS_DIR.resolve(base + "_" + counter + ext);
				counter++;
			}
			Files.copy(source, target);
			scan();
			return target.getFileName().toString();
		} catch (Exception e) {
			ItemDespawnClient.LOGGER.warn("导入音频失败: {}", source, e);
			return null;
		}
	}
}
