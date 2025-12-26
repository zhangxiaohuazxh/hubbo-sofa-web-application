package cn.hubbo.unit;

import org.junit.jupiter.api.Test;

public class NPEUnitTest {

	@Test
	void testCheck4JSpecifyAnnotation() {
		// 调用处有警告提示 pass
		String res = fun(null);
		System.out.println(res);
	}

	private String fun(String str) {
		return str;
	}

}
