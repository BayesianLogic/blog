package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import blog.engine.onlinePF.Util.FileCommunicator;

public class UBT {
	public static boolean dropHistory = true;
	public static boolean debug = true;
	public static boolean rememberHistory = true;
	public static enum schemes {allVariables, nonObservableVariables, hiddenVariables};
	public static schemes currentScheme = schemes.hiddenVariables;
	public static Stopwatch runTimeTimer = new Stopwatch();
	public static ArrayList<Double> timingData = new ArrayList<Double>();
	public static ArrayList<Integer> numStateData = new ArrayList<Integer>();
	public static ArrayList<Double> valueData = new ArrayList<Double>();
	public static boolean particleCoupling = false; //THIS WILL NOT WORK till you fix abstractpartialworld.equals and .hashcode (condition checks for schemes)
	public static Stopwatch specialTimer = new Stopwatch();
	public static Double specialTimingData = 0.0;//timing for equals world
	public static Double specialTimingData2 = 0.0;//timing for hashcode world
	public static Double specialTimingData3 = 0.0;//timing for equals observability sig
	public static Double specialTimingData4 = 0.0;//timing for hashcode observability sig
	public static Double specialTimingData5 = 0.0;//timing for copy/answer with particles
	public static Double specialTimingData6 = 0.0;//timing for answering in each bucket
	public static FileCommunicator dataOutput = new FileCommunicator("randomstuff//UBTData.log");
        public static FileCommunicator osOutput = new FileCommunicator("randomstuff//OS.log");
        public static FileCommunicator worldOutput;// = new FileCommunicator("randomstuff//World.log");
        public static FileCommunicator valueOutput;// = new FileCommunicator("randomstuff//World.log");
        public static int numtstep;
	//public static FileCommunicator dataOutput2 = new FileCommunicator("tiger2Data.txt");
	public static double answerTime=0;
	public static double emptyCacheTime=0;
	public static double takeWithPartitionTime=0;
	public static double resampleTime=0;
	public static double repartitionTime=0;
	public static double resamplePartitionAndParticlesTime=0;
	public static void outputRunTime(){
		timingProfile.printInput("answerTime: "+answerTime);
		timingProfile.printInput("emptyCacheTime: "+emptyCacheTime);
		timingProfile.printInput("takeWithPartitionTime: "+takeWithPartitionTime);
		timingProfile.printInput("resampleTime: "+resampleTime);
		timingProfile.printInput("repartitionTime: "+repartitionTime);
		timingProfile.printInput("resamplePartitionAndParticlesTime: "+resamplePartitionAndParticlesTime);
		answerTime = 0;
		emptyCacheTime = 0;
		takeWithPartitionTime = 0;
		resampleTime = 0;
		repartitionTime = 0;
		resamplePartitionAndParticlesTime = 0;
	}
	public static FileCommunicator timingProfile = new FileCommunicator("randomstuff//timingProfile.log");
	public static FileCommunicator valueOutput2;
	public static FileCommunicator valueOutput3;
	public static FileCommunicator varianceOutput;
	public static FileCommunicator varianceOutput2;
	public static FileCommunicator varianceOutput3;
	//public static FileCommunicator specialIndexOutput;
	//public static FileCommunicator particleHistogramOutput;
	//public static FileCommunicator obsOutput;
	//public static BufferedReader obsReader;
	//public static boolean singleObs = false;
	
	
	
	
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
