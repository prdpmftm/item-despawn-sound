package com.mtwork.itemdespawn;

import com.mtwork.itemdespawn.audio.TrackLibrary;
import com.mtwork.itemdespawn.keybind.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDespawnClient implements ClientModInitializer {
	public static final String MOD_ID = "itemdespawn";
	public static final Logger LOGGER = LoggerFactory.getLogger("ItemDespawnSound");

	@Override
	public void onInitializeClient() {
		DespawnConfig.load();
		TrackLibrary.scan();
		Keybinds.init();
		LOGGER.info("物品消失音效已加载，音轨 {} 个", TrackLibrary.names().size());
	}
}
