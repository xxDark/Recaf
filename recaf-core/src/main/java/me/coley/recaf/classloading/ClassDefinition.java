package me.coley.recaf.classloading;

import me.coley.recaf.io.ByteSource;
import me.coley.recaf.io.ByteSources;
import org.objectweb.asm.ClassReader;

import java.io.IOException;

/**
 * Class definition.
 *
 * @author xDark
 */
public final class ClassDefinition {
	private ByteSource bytecode;
	private ClassReader reader;
	private byte[] raw;

	/**
	 * @param bytecode
	 * 		Class bytecode.
	 */
	public ClassDefinition(ByteSource bytecode) {
		this.bytecode = bytecode;
	}

	/**
	 * @return Raw bytecode.
	 *
	 * @throws IOException
	 * 		If any I/O error occurs.
	 */
	public byte[] getRawBytecode() throws IOException {
		byte[] raw = this.raw;
		if (raw == null) {
			raw = bytecode.readAll();
			bytecode = ByteSources.wrap(raw);
			this.raw = raw;
		}
		return raw;
	}

	/**
	 * @return Class bytes.
	 */
	public ByteSource getBytecode() {
		return bytecode;
	}

	/**
	 * @return Lazily created class reader.
	 */
	public ClassReader getReader() throws IOException {
		ClassReader reader = this.reader;
		if (reader == null) {
			reader = new ClassReader(getRawBytecode());
			this.reader = reader;
		}
		return reader;
	}
}
