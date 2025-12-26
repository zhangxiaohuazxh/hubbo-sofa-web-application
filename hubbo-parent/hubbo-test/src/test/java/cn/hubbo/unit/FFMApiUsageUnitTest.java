package cn.hubbo.unit;

import cn.hubbo.nativebridge.invocation.custom.MathLibInvocation;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

public class FFMApiUsageUnitTest {

	@Disabled
	@RepeatedTest(30000)
	void testFFMDemo() {
		Stopwatch stopwatch = Stopwatch.createStarted();
		MathLibInvocation.pow();
		long millis = stopwatch.elapsed().toMillis();
		System.out.println("执行耗时 " + millis + " ms");
	}

}
