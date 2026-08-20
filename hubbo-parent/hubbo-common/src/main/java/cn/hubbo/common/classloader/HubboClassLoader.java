package cn.hubbo.common.classloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从可配置目录按类名加载 class 字节码的类加载器。
 * <p>
 * 字节码根目录通过系统属性 {@code hubbo.classloader.dir} 配置，默认 <code>classes</code>（相对工作目录）；
 * 目录结构遵循标准包路径（如 <code>cn/hubbo/Demo.class</code>）。
 * </p>
 */
public class HubboClassLoader extends ClassLoader {

	private static final Map<String, Class<?>> CACHE = new ConcurrentHashMap<>();

	private final Path classPath;

	public HubboClassLoader(Path classPath) {
		this.classPath = classPath;
	}

	public HubboClassLoader() {
		this(Paths.get(System.getProperty("hubbo.classloader.dir", "classes")));
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> cached = CACHE.get(name);
		if (cached != null) {
			return cached;
		}
		return super.loadClass(name);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Path classFile = classPath.resolve(name.replace('.', '/') + ".class");
		if (!Files.isRegularFile(classFile)) {
			throw new ClassNotFoundException(name);
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(classFile);
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
		Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
		CACHE.put(name, clazz);
		return clazz;
	}

}