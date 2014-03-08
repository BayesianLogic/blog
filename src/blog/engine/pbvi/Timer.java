package blog.engine.pbvi;

/**
 * To be moved or replaced by something else.
 *
 */
public class Timer {
	static long startTime = 0;
	
	public static void start() {
		startTime = System.currentTimeMillis();
	}
	
	public static long getElapsed() {
		return System.currentTimeMillis() - startTime;
	}
}
