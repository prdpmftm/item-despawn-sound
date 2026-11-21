package com.mtwork.itemdespawn.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.minecraft.client.sounds.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 用 JLayer 解码 mp3，给游戏提供 16 位小端 PCM 流。
 */
public class Mp3AudioStream implements AudioStream {
	private final Bitstream bitstream;
	private final Decoder decoder = new Decoder();
	private AudioFormat format;
	private byte[] pending = new byte[0];
	private int pendingOffset = 0;
	private boolean eof = false;

	public Mp3AudioStream(InputStream input) throws IOException {
		this.bitstream = new Bitstream(new BufferedInputStream(input, 16384));
		try {
			while (this.format == null) {
				Header header = this.bitstream.readFrame();
				if (header == null) {
					throw new IOException("mp3 中没有可解码的音频帧");
				}
				SampleBuffer buffer = (SampleBuffer) this.decoder.decodeFrame(header, this.bitstream);
				int length = buffer.getBufferLength();
				if (length > 0) {
					this.format = new AudioFormat(buffer.getSampleFrequency(), 16,
							buffer.getChannelCount(), true, false);
					this.pending = toBytes(buffer.getBuffer(), length);
					this.pendingOffset = 0;
				}
				this.bitstream.closeFrame();
			}
		} catch (BitstreamException | DecoderException e) {
			throw new IOException("解码 mp3 失败", e);
		}
	}

	public Mp3AudioStream(File file) throws IOException {
		this(new FileInputStream(file));
	}

	private static byte[] toBytes(short[] samples, int length) {
		byte[] out = new byte[length * 2];
		for (int i = 0; i < length; i++) {
			short value = samples[i];
			out[i * 2] = (byte) (value & 0xFF);
			out[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
		}
		return out;
	}

	private boolean decodeNextFrame() {
		try {
			while (true) {
				Header header = this.bitstream.readFrame();
				if (header == null) {
					this.eof = true;
					return false;
				}
				SampleBuffer buffer = (SampleBuffer) this.decoder.decodeFrame(header, this.bitstream);
				int length = buffer.getBufferLength();
				if (length > 0) {
					this.pending = toBytes(buffer.getBuffer(), length);
					this.pendingOffset = 0;
				}
				this.bitstream.closeFrame();
				if (length > 0) {
					return true;
				}
			}
		} catch (BitstreamException | DecoderException e) {
			this.eof = true;
			return false;
		}
	}

	@Override
	public AudioFormat getFormat() {
		return this.format;
	}

	@Override
	public ByteBuffer read(int size) throws IOException {
		if (this.eof && this.pendingOffset >= this.pending.length) {
			return null;
		}
		// 必须用直接缓冲区：OpenAL 的 alBufferData 直接读这块内存，堆缓冲区会崩溃
		ByteBuffer out = ByteBuffer.allocateDirect(size);
		while (out.position() < size) {
			if (this.pendingOffset >= this.pending.length) {
				if (!this.decodeNextFrame()) {
					break;
				}
			}
			int count = Math.min(size - out.position(), this.pending.length - this.pendingOffset);
			out.put(this.pending, this.pendingOffset, count);
			this.pendingOffset += count;
		}
		if (out.position() == 0) {
			return null;
		}
		out.flip();
		return out;
	}

	@Override
	public void close() throws IOException {
		try {
			this.bitstream.close();
		} catch (BitstreamException e) {
			throw new IOException(e);
		}
	}
}
