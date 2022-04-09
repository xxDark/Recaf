package me.coley.recaf.io;

import software.coley.llzip.util.ByteData;
import software.coley.llzip.util.ByteDataUtil;

import java.io.IOException;

/**
 * Byte container that allows delayed
 * reads.
 * 
 * @author xDark
 */
public interface ByteContainer {

	/**
	 * Reads all bytes from the container.
	 *
	 * @return Read bytes.
	 *
	 * @throws IOException
	 * 		If any I/O error occurs.
	 */
	byte[] readAll() throws IOException;

	static ByteContainer just(byte[] array) {
		return () -> array;
	}

	static ByteContainer forByteData(ByteData data) {
		return () -> ByteDataUtil.toByteArray(data);
	}
}
