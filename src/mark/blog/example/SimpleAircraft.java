package mark.blog.example;

import java.util.HashMap;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Poisson;

/**
 * Native implementation of simple-aircraft inference algorithm.
 * Used for a performance comparison with BLOG.
 * 
 * @author awong
 */
public class SimpleAircraft {
	// Parameters for model
	private static final double MEAN_AIRCRAFT = 5.0;
	private static final double MEAN_BLIPS = 4.0;
	private static final int NUM_BLIPS = 3;
	
	// Parameters for inference algorithm
	private static final int NUM_SAMPLES = 50000;
	
	// Distributions for sampling in model
	private Poisson aircraftGen;
	private Poisson blipGen;
	
	public SimpleAircraft() {
		Util.initRandom(false);
		aircraftGen = new Poisson(MEAN_AIRCRAFT);
		blipGen = new Poisson(MEAN_BLIPS);
	}
	
	public int sampleAircraft() {
		return aircraftGen.sampleInt();
	}
	
	public int sampleBlip() {
		return blipGen.sampleInt();
	}
	
	public static void main(String[] args) {
		SimpleAircraft model = new SimpleAircraft();
		Map<Integer, Integer> samples = new HashMap<Integer, Integer>();
		int samplesAccepted = 0;
		
		for (int i = 0; i < NUM_SAMPLES; i++) {
			int numBlips = 0;
			int numAircraft = model.sampleAircraft();
			for (int j = 0; j < numAircraft; j++) {
				numBlips += model.sampleBlip();
			}
			
			if (numBlips == NUM_BLIPS) {
				samplesAccepted++;
				if (samples.containsKey(numAircraft)) {
					samples.put(numAircraft, samples.get(numAircraft) + 1);
				} else {
					samples.put(numAircraft, 1);
				}
			}
		}
		
		System.out.println("Number of samples accepted: " + samplesAccepted);
		if (samplesAccepted > 0) {
			for (Integer num: samples.keySet()) {
				System.out.println(num + ":\t" + (double)samples.get(num) / samplesAccepted);
			}
		}
	}
}
