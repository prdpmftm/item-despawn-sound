package com.mtwork.itemdespawn.gui;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * 系统原生文件选择对话框（LWJGL tinyfd）。没有 zenity 等组件时返回 null。
 */
public final class FileDialogs {
	private FileDialogs() {
	}

	/** 选择 ogg 文件，多个用 | 分隔；取消返回 null。 */
	public static String pickOgg(String defaultDir) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer filters = stack.mallocPointer(2);
			filters.put(stack.UTF8("*.ogg"));
			filters.put(stack.UTF8("*.mp3"));
			filters.flip();
			return TinyFileDialogs.tinyfd_openFileDialog("选择要导入的音频（.ogg / .mp3）", defaultDir + "/",
					filters, "音频 (*.ogg, *.mp3)", true);
		} catch (Throwable t) {
			return null;
		}
	}
}
