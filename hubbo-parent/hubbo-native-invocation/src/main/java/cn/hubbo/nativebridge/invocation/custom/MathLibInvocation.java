package cn.hubbo.nativebridge.invocation.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 调用 Rust 原生库（hubbo-native）的数学函数。
 * <p>
 * 动态库命名随平台变化：Windows 为 {@code .dll}、macOS 为 {@code .dylib}、
 * Linux 为 {@code .so}，按当前系统自动选择。
 * </p>
 */
public final class MathLibInvocation {

	private static final Logger log = LoggerFactory.getLogger(MathLibInvocation.class);

	private static final Linker LINKER = Linker.nativeLinker();

	private static final MethodHandle METHOD_HANDLE;

	private static final Arena ARENA;

	static {
		ARENA = Arena.ofShared();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				ARENA.close();
			} catch (IllegalStateException e) {
				// Arena 已关闭或当前不可关闭，忽略
			}
		}, "hubbo-native-arena-cleanup"));

		Path path;
		MethodHandle tempMethodHandle = null;
		try {
			path = resolveNativeLibraryPath();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Failed to resolve native library location", e);
		}
		if (path.toFile().exists()) {
			try {
				SymbolLookup symbolLookup = SymbolLookup.libraryLookup(path, ARENA);
				FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
				MemorySegment memorySegment = symbolLookup.find("pow").orElseThrow();
				tempMethodHandle = LINKER.downcallHandle(memorySegment, descriptor);
				log.info("成功加载动态库 {}", path);
			} catch (Exception e) {
				log.error("加载动态库失败: {}", path, e);
			}
		} else {
			log.warn("没有找到动态库: {}", path);
		}
		METHOD_HANDLE = tempMethodHandle;
	}

	/** 按平台选择动态库文件名。 */
	private static Path resolveNativeLibraryPath() throws URISyntaxException {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		String libName;
		if (os.contains("win")) {
			libName = "hubbo_native_dynamic_lib.dll";
		} else if (os.contains("mac")) {
			libName = "libhubbo_native_dynamic_lib.dylib";
		} else {
			libName = "libhubbo_native_dynamic_lib.so";
		}
		URL location = MathLibInvocation.class.getProtectionDomain().getCodeSource().getLocation();
		return Paths.get(location.toURI()).resolve("lib").resolve(libName);
	}

	public static void pow() {
		try {
			if (METHOD_HANDLE == null) {
				log.warn("执行失败，未找到动态库");
				return;
			}
			int res = (int) METHOD_HANDLE.invokeExact(2, 3);
			log.info("执行结果 {}", res);
		} catch (Throwable e) {
			if (e instanceof Error) {
				throw (Error) e;
			}
			log.error("调用native pow函数出错", e);
		}
	}


}