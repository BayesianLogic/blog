package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.Timer;

public class UniversalBenchmarkTool {
	public static boolean rememberHistory = false;
	public static enum schemes {allVariables, nonObservableVariables, hiddenVariables};
	public static schemes currentScheme = schemes.hiddenVariables;
	public static Stopwatch runTimeTimer = new Stopwatch();
	public static ArrayList<Double> timingData = new ArrayList<Double>();
	public static ArrayList<Integer> numStateData = new ArrayList<Integer>();
	public static ArrayList<Double> valueData = new ArrayList<Double>();
	public static boolean particleCoupling = false;
	
	public static class Stopwatch { 

	    private long start;

	    public Stopwatch() {
	        start = System.currentTimeMillis();
	    }
	    
	    public void startTimer(){
	    	start = System.currentTimeMillis();
	    }

	    // return time (in seconds) since this object was created
	    public double elapsedTime() {
	        long now = System.currentTimeMillis();
	        return (now - start) / 1000.0;
	    } 
	}
}
