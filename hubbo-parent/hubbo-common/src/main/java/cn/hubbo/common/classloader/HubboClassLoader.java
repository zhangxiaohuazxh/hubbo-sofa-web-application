package cn.hubbo.common.classloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * todo 待实现
 */
public class HubboClassLoader extends ClassLoader {

	private static final Map<String, Class<?>> CACHE = new HashMap<>();

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = super.loadClass(name);
		if (clazz == null) {
			clazz = findClass(name);
		}
		return clazz;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\33233\\Downloads\\tmp\\Demo.class"));
			Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
			CACHE.put(name, clazz);
			return clazz;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
