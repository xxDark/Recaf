package dev.xdark.recaf.mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public final class MixinPropertyServiceRecaf implements IGlobalPropertyService {
	private final Map<IPropertyKey, Object> map = new HashMap<>();


	@Override
	public IPropertyKey resolveKey(String name) {
		return new Key(name);
	}

	@Override
	public <T> T getProperty(IPropertyKey key) {
		return (T) map.get(key);
	}

	@Override
	public void setProperty(IPropertyKey key, Object value) {
		map.put(key, value);
	}

	@Override
	public <T> T getProperty(IPropertyKey key, T defaultValue) {
		return (T) map.getOrDefault(key, defaultValue);
	}

	@Override
	public String getPropertyString(IPropertyKey key, String defaultValue) {
		return (String) map.getOrDefault(key, defaultValue);
	}

	private static final class Key implements IPropertyKey {

		private final String name;

		Key(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return name.equals(((Key) o).name);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
}
