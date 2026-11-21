package com.mtwork.itemdespawn.mixin;

import com.mtwork.itemdespawn.audio.DespawnSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端移除实体时检查是不是掉落物到期消失（原版 5 分钟 = 6000 tick）。
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(method = "removeEntity", at = @At("HEAD"))
	private void itemdespawn$onRemoveEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
		if (reason != Entity.RemovalReason.DISCARDED) {
			return;
		}
		ClientLevel level = (ClientLevel) (Object) this;
		Entity entity = level.getEntity(entityId);
		if (entity instanceof ItemEntity item) {
			DespawnSounds.onItemRemoved(item);
		}
	}
}
