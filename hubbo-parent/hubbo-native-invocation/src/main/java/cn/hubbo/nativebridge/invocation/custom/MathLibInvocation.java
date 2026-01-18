package cn.hubbo.nativebridge.invocation.custom;

import lombok.extern.slf4j.Slf4j;
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


public final class MathLibInvocation {

	private static final Logger log = LoggerFactory.getLogger(MathLibInvocation.class);

	static final Linker LINKER = Linker.nativeLinker();

	static final MethodHandle METHOD_HANDLE;

	static final Arena ARENA = Arena.ofShared();


	static {
		URL location = MathLibInvocation.class.getProtectionDomain().getCodeSource().getLocation();
		Path path = null;
		MethodHandle tempMethodHandle = null;
		try {
			path = Paths.get(location.toURI()).resolve("lib/hubbo_native_dynamic_lib.dll");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		if (path.toFile().exists()) {
			SymbolLookup symbolLookup = SymbolLookup.libraryLookup(path, ARENA);
			FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
			MemorySegment memorySegment = symbolLookup.find("pow").orElseThrow();
			tempMethodHandle = LINKER.downcallHandle(memorySegment, descriptor);
			log.info("成功加载动态库");
		} else {
			log.warn("没有找到动态库");
		}
		METHOD_HANDLE = tempMethodHandle;
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
			log.error("调用native pow函数出错", e);
		}
	}


}
