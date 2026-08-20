package cn.hubbo.common.classloader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 从可配置目录按类名加载 class 字节码的类加载器。
 * <p>
 * 字节码根目录通过系统属性 {@code hubbo.classloader.dir} 配置，默认 <code>classes</code>（相对工作目录）；
 * 目录结构遵循标准包路径（如 <code>cn/hubbo/Demo.class</code>）。
 * </p>
 * <p>
 * 缓存采用有界 + 弱引用：条目数上限为 {@value #MAX_CACHE_SIZE}，
 * 且 Class 对象不被强引用时允许随其类加载器一起被回收，避免类加载器内存泄漏。
 * </p>
 */
public class HubboClassLoader extends ClassLoader {

	private static final int MAX_CACHE_SIZE = 512;

	private static final Cache<String, Class<?>> CACHE = CacheBuilder.newBuilder()
		.maximumSize(MAX_CACHE_SIZE)
		.weakValues()
		.build();

	private final Path classPath;

	public HubboClassLoader(Path classPath) {
		this.classPath = classPath;
	}

	public HubboClassLoader() {
		this(Paths.get(System.getProperty("hubbo.classloader.dir", "classes")));
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> cached = CACHE.getIfPresent(name);
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