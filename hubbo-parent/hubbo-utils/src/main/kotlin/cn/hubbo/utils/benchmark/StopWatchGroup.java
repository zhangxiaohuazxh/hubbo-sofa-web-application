package cn.hubbo.utils.benchmark;

import com.google.common.collect.Maps;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class StopWatchGroup {

	public static StopWatch start(String name) {
		//new StopWatch()
		return null;
	}

	void stop(String name) {

	}




	public static Map<String, StopWatch> getTasks() {
		HashMap<String, StopWatch> map = Maps.newHashMap();
		return map;
	}


}
