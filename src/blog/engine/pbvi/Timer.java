package blog.engine.pbvi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * To be moved or replaced by something else. common/Timer?
 *
 */
public class Timer {
	static boolean off = false;
	
	static long startTime = 0;
	static Map<String, Long> startTimes = new HashMap<String, Long>();
	static Map<String, Long> aggregatedTimes = new HashMap<String, Long>();
	static Map<String, Long> longestTimes = new HashMap<String, Long>();
	static Map<String, Long> numRecords = new HashMap<String, Long>();
	static DateFormat formatter;
	static  {
		formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		
	}
	public static void start() {
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
		return System.currentTimeMillis() - startTime;
	}
	
	public static String getElapsedStr() {
		return niceTimeString(getElapsed());
	}
	
	public static void print() {
		if (!off) {
			System.out.println("Aggregated: " + timeStr(aggregatedTimes) + 
					"\nLongest: " + timeStr(longestTimes) + 
					"\n# Records: " + timeStr(numRecords));
		}
	}
	
	public static String timeStr(Map<String, Long> times) {
		String result = "";
		for (String s : times.keySet()) {
			result += s + " " + niceTimeString(times.get(s)) + "\n";
		}
		return result;
	}

	public static long getAggregate(String string) {
		Long result = aggregatedTimes.get(string);
		if (result == null) return 0;
		return result;
	}
	
	public static String niceTimeString(Long ms) {
		long h = ms/(1000 * 60 * 60);
		long m = (ms % (1000 * 60 * 60))/(60 * 1000);
		long s = (ms % (60 * 1000)) / 1000;
		ms = ms % 1000;
		return h + "h" + m + "m" + s + "s" + ms + "ms";
	}
}
