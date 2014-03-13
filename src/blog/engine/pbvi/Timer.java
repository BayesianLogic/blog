package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;

/**
 * To be moved or replaced by something else. common/Timer?
 *
 */
public class Timer {
	static boolean off = true;
	
	static long startTime = 0;
	static Map<String, Long> startTimes = new HashMap<String, Long>();
	static Map<String, Long> aggregatedTimes = new HashMap<String, Long>();
	static Map<String, Long> longestTimes = new HashMap<String, Long>();
	static Map<String, Long> numRecords = new HashMap<String, Long>();
	
	public static void start() {
		if (!off)
			startTime = System.currentTimeMillis();
	}
	
	public static void start(String s) {
		if (!off)
			startTimes.put(s, System.currentTimeMillis());
	}
	
	public static void record(String s) {
		if (!off) {
			long elapsed = System.currentTimeMillis() - startTimes.get(s);
			if (!aggregatedTimes.containsKey(s)) {
				aggregatedTimes.put(s, 0L);
			}
			if (!longestTimes.containsKey(s)) {
				longestTimes.put(s, 0L);
			}
			if (!numRecords.containsKey(s)) {
				numRecords.put(s, 0L);
			}

			aggregatedTimes.put(s, aggregatedTimes.get(s) + elapsed);
			if (elapsed > longestTimes.get(s))
				longestTimes.put(s, elapsed);
			numRecords.put(s, numRecords.get(s) + 1);
		}
	}
	
	public static long getElapsed() {
		if (!off)
			return System.currentTimeMillis() - startTime;
		else 
			return 0L;
	}
	
	public static void print() {
		if (!off) {
		System.out.println("Aggregated: " + aggregatedTimes.toString() + 
				"\nLongest: " + longestTimes + 
				"\n# Records: " + numRecords);
		}
	}
}
