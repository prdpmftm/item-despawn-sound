package com.mtwork.itemdespawn.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import com.mtwork.itemdespawn.audio.TrackLibrary;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 让自定义音轨直接从 config 文件夹读取（ogg 走游戏解码器，mp3 走 JLayer），而不是走资源包。
 */
@Mixin(SoundBufferLibrary.class)
public abstract class SoundBufferLibraryMixin {
	@Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
	private void itemdespawn$getStream(ResourceLocation id, boolean looping,
			CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
		Path path = TrackLibrary.pathFor(id);
		if (path == null) {
			return;
		}
		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			try {
				return TrackLibrary.open(path);
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}));
	}

	@Inject(method = "getCompleteBuffer", at = @At("HEAD"), cancellable = true)
	private void itemdespawn$getCompleteBuffer(ResourceLocation id,
			CallbackInfoReturnable<CompletableFuture<SoundBuffer>> cir) {
		Path path = TrackLibrary.pathFor(id);
		if (path == null) {
			return;
		}
		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			try (AudioStream stream = TrackLibrary.open(path)) {
				ByteBuffer data = TrackLibrary.readAll(stream);
				return new SoundBuffer(data, stream.getFormat());
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}));
	}
}
