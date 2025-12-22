package cn.hubbo;

import cn.hubbo.common.classloader.HubboClassLoader;
import org.junit.jupiter.api.Test;

public class ClassLoaderUnitTest {


	@Test
	public void testHubboClassLoader() throws ClassNotFoundException {
		HubboClassLoader classLoader = new HubboClassLoader();
		HubboClassLoader classLoader2 = new HubboClassLoader();
		Class<?> clazz = classLoader.loadClass("cn.hubbo.classloader.test.Demo");
		Class<?> clazz2 = classLoader.loadClass("cn.hubbo.classloader.test.Demo");
		System.out.println(clazz == clazz2);
	}


}
