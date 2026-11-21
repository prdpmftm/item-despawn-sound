package com.mtwork.itemdespawn.audio;

import com.mtwork.itemdespawn.DespawnConfig;
import com.mtwork.itemdespawn.ItemDespawnClient;
import com.mtwork.itemdespawn.subtitle.SubtitleOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * 判定与播放逻辑。
 */
public final class DespawnSounds {
	private static long lastPlayedAt = 0L;

	private DespawnSounds() {
	}

	/** 客户端检测到物品实体被移除时调用。 */
	public static void onItemRemoved(ItemEntity item) {
		DespawnConfig cfg = DespawnConfig.get();
		if (!cfg.enabled) {
			return;
		}
		if (!cfg.lenient && item.tickCount < cfg.minAgeTicks) {
			return;
		}
		if (cfg.showsSubtitle()) {
			SubtitleOverlay.push(item.getItem());
		}
		if (!cfg.playsSound()) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastPlayedAt < cfg.cooldownMs) {
			return;
		}
		lastPlayedAt = now;
		play(item.getX(), item.getY(), item.getZ());
	}

	/** 按当前配置在指定坐标播放一次。 */
	public static void play(double x, double y, double z) {
		DespawnConfig cfg = DespawnConfig.get();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.getSoundManager() == null) {
			return;
		}
		String name = cfg.track;
		if (TrackLibrary.has(name)) {
			Sound sound = TrackLibrary.sound(name);
			SoundInstance instance = new LocalTrackSoundInstance(sound.getLocation(), sound,
					(float) cfg.volume, (float) cfg.pitch, x, y, z);
			minecraft.getSoundManager().play(instance);
		} else {
			if (name != null && !name.isEmpty()) {
				ItemDespawnClient.LOGGER.debug("音轨 {} 不存在，回退到原版音效", name);
			}
			minecraft.level.playLocalSound(x, y, z, SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS,
					(float) cfg.volume, (float) cfg.pitch, false);
		}
	}

	/** 配置界面试听：在玩家位置播放。 */
	public static void preview() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) {
			return;
		}
		DespawnConfig cfg = DespawnConfig.get();
		String name = cfg.track;
		if (TrackLibrary.has(name)) {
			Sound sound = TrackLibrary.sound(name);
			SoundInstance instance = new LocalTrackSoundInstance(sound.getLocation(), sound,
					(float) cfg.volume, (float) cfg.pitch,
					minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
			if (minecraft.getSoundManager() != null) {
				minecraft.getSoundManager().play(instance);
			}
		} else if (minecraft.level != null) {
			minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(),
					SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS,
					(float) cfg.volume, (float) cfg.pitch, false);
		}
	}
}
