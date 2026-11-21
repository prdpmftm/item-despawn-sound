package com.mtwork.itemdespawn.audio;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

/**
 * 播放磁盘上自定义 ogg 的 SoundInstance。
 */
public class LocalTrackSoundInstance implements SoundInstance {
	private final ResourceLocation id;
	private final Sound sound;
	private final float volume;
	private final float pitch;
	private final double x;
	private final double y;
	private final double z;

	public LocalTrackSoundInstance(ResourceLocation id, Sound sound, float volume, float pitch, double x, double y, double z) {
		this.id = id;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public ResourceLocation getLocation() {
		return this.id;
	}

	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager) {
		WeighedSoundEvents events = new WeighedSoundEvents(this.id, null);
		events.addSound(this.sound);
		return events;
	}

	@Override
	public Sound getSound() {
		return this.sound;
	}

	@Override
	public SoundSource getSource() {
		return SoundSource.PLAYERS;
	}

	@Override
	public boolean isLooping() {
		return false;
	}

	@Override
	public boolean isRelative() {
		return false;
	}

	@Override
	public int getDelay() {
		return 0;
	}

	@Override
	public float getVolume() {
		return this.volume;
	}

	@Override
	public float getPitch() {
		return this.pitch;
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	@Override
	public double getZ() {
		return this.z;
	}

	@Override
	public Attenuation getAttenuation() {
		return Attenuation.LINEAR;
	}
}
