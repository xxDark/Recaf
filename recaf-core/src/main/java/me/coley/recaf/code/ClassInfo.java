package me.coley.recaf.code;

import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Class info for resource. Provides some basic information about the class.
 *
 * @author Matt Coley
 */
public class ClassInfo implements ItemInfo, LiteralInfo, CommonClassInfo {
	private final byte[] value;
	private final String name;
	private final String superName;
	private final List<String> interfaces;
	private final int version;
	private final int access;
	private final List<FieldInfo> fields;
	private final List<MethodInfo> methods;

	private ClassInfo(String name, String superName, List<String> interfaces, int version, int access,
					  List<FieldInfo> fields, List<MethodInfo> methods, byte[] value) {
		this.value = value;
		this.name = name;
		this.superName = superName;
		this.interfaces = interfaces;
		this.version = version;
		this.access = access;
		this.fields = fields;
		this.methods = methods;
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSuperName() {
		return superName;
	}

	@Override
	public List<String> getInterfaces() {
		return interfaces;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public List<FieldInfo> getFields() {
		return fields;
	}

	@Override
	public List<MethodInfo> getMethods() {
		return methods;
	}

	/**
	 * @return Class major version.
	 */
	public int getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassInfo info = (ClassInfo) o;
		return access == info.access &&
				Objects.equals(name, info.name) &&
				Objects.equals(superName, info.superName) &&
				Objects.equals(interfaces, info.interfaces) &&
				Objects.equals(fields, info.fields) &&
				Objects.equals(methods, info.methods);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, superName, interfaces, access, fields, methods);
	}

	/**
	 * Create a class info unit from the given class bytecode.
	 *
	 * @param value
	 * 		Class bytecode.
	 *
	 * @return Parsed class information unit.
	 */
	public static ClassInfo read(byte[] value) {
		ClassReader reader = new ClassReader(value);
		String className = reader.getClassName();
		String superName = reader.getSuperName();
		List<String> interfaces = Arrays.asList(reader.getInterfaces());
		int access = reader.getAccess();
		int version = reader.readInt(reader.getItem(1) - 7);
		char[] buffer = new char[reader.getMaxStringLength()];
		int currentOffset = reader.header;
		// Skip access flag, this, super, interface count.
		currentOffset += 8;
		currentOffset += (2 * interfaces.size()); // SKip interfaces.
		int fieldCount = reader.readUnsignedShort(currentOffset);
		List<FieldInfo> fields = new ArrayList<>(fieldCount);
		currentOffset += 2;
		currentOffset = readFields(reader, fields, className, fieldCount, currentOffset, buffer);
		int methodCount = reader.readUnsignedShort(currentOffset);
		List<MethodInfo> methods = new ArrayList<>(methodCount);
		currentOffset += 2;
		readMethods(reader, methods, className, methodCount, currentOffset, buffer);
		return new ClassInfo(
				className,
				superName,
				interfaces,
				version,
				access,
				fields,
				methods,
				value);
	}

	private static int readFields(ClassReader reader,
								  List<FieldInfo> fields,
								  String className,
								  int fieldCount,
								  int currentOffset, char[] buffer) {
		for (int i = 0; i < fieldCount; i++) {
			int fieldAccess = reader.readUnsignedShort(currentOffset);
			currentOffset += 2;
			String name = reader.readUTF8(currentOffset, buffer);
			currentOffset += 2;
			String desc = reader.readUTF8(currentOffset, buffer);
			currentOffset += 2;
			currentOffset = skipAttributes(reader, currentOffset);
			fields.add(new FieldInfo(className, name, desc, fieldAccess));
		}
		return currentOffset;
	}

	private static void readMethods(ClassReader reader,
								  List<MethodInfo> fields,
								  String className,
								  int fieldCount,
								  int currentOffset, char[] buffer) {
		for (int i = 0; i < fieldCount; i++) {
			int fieldAccess = reader.readUnsignedShort(currentOffset);
			currentOffset += 2;
			String name = reader.readUTF8(currentOffset, buffer);
			currentOffset += 2;
			String desc = reader.readUTF8(currentOffset, buffer);
			currentOffset += 2;
			currentOffset = skipAttributes(reader, currentOffset);
			fields.add(new MethodInfo(className, name, desc, fieldAccess));
		}
	}

	private static int skipAttributes(ClassReader reader, int currentOffset) {
		int attributes = reader.readUnsignedShort(currentOffset);
		currentOffset += 2;
		while (attributes-- != 0) {
			currentOffset += 2;
			currentOffset += reader.readInt(currentOffset) + 4;
		}
		return currentOffset;
	}
}
