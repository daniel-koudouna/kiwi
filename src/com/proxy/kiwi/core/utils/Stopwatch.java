package com.proxy.kiwi.core.utils;

import java.util.HashMap;

public class Stopwatch {
	private static HashMap<String, Long> timers = new HashMap<>();

	public static void click(String s) {
		if (timers.containsKey(s)) {
			Log.print(Log.TIME, s + " took " + (System.currentTimeMillis() - timers.remove(s)) / 1000.0 + " seconds.");
		} else {
			timers.put(s, System.currentTimeMillis());
			Log.print(Log.TIME, s + "...");
		}
	}
}
