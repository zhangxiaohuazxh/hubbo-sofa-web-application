package cn.hubbo;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class DiffUnitTest {

	@Test
	public void testSimpleDiff() {
		List<String> lines = Lists.newArrayList();
		lines.add("1");
		lines.add("2");
		lines.add("3");
		List<String> anotherLines = Lists.newArrayList();
		anotherLines.add("a");
		anotherLines.add("b");
		anotherLines.add("c");
		Patch<String> patch = DiffUtils.diff(lines, anotherLines);
		for (AbstractDelta<String> delta : patch.getDeltas()) {
			System.out.println(delta.getType());
			System.out.println(delta.getSource());
			System.out.println(delta.getTarget());
		}
	}

}
