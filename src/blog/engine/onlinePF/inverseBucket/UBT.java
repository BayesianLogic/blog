package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.Timer;

import blog.engine.onlinePF.FileCommunicator;

public class UBT {
	public static boolean debug = true;
	public static boolean rememberHistory = true;
	public static enum schemes {allVariables, nonObservableVariables, hiddenVariables};
	public static schemes currentScheme = schemes.nonObservableVariables;
	public static Stopwatch runTimeTimer = new Stopwatch();
	public static ArrayList<Double> timingData = new ArrayList<Double>();
	public static ArrayList<Integer> numStateData = new ArrayList<Integer>();
	public static ArrayList<Double> valueData = new ArrayList<Double>();
	public static boolean particleCoupling = false;
	public static Stopwatch specialTimer = new Stopwatch();
	public static Double specialTimingData = 0.0;//timing for equals world
	public static Double specialTimingData2 = 0.0;//timing for hashcode world
	public static Double specialTimingData3 = 0.0;//timing for equals observability sig
	public static Double specialTimingData4 = 0.0;//timing for hashcode observability sig
	public static Double specialTimingData5 = 0.0;//timing for copy/answer with particles
	public static Double specialTimingData6 = 0.0;//timing for answering in each bucket
	public static FileCommunicator dataOutput = new FileCommunicator("tiger2Data.txt");
	//public static FileCommunicator dataOutput2 = new FileCommunicator("tiger2Data.txt");
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
