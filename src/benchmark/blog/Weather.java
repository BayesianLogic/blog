package benchmark.blog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Bernoulli;

public class Weather {
	/* Particle */
	class Particle {
		private boolean isRainyRegion;
		private boolean isRaining;
		private double weight;
		
		public Particle() {
			isRainyRegion = drawRainyRegion.sampleVal();
			isRaining = (isRainyRegion) ? drawInitialRainyIfRR.sampleVal() : drawInitialRainyIfNotRR.sampleVal();
			weight = 1.0;
		}
		
		private Particle(boolean regionRainy, boolean currentlyRaining) {
			isRainyRegion = regionRainy;
			isRaining = currentlyRaining;
			weight = 1.0;
		}
		
		public Particle propagate() {
			boolean nextRain;
			if (isRainyRegion) {
				nextRain = (isRaining) ? drawRainyIfRainingRR.sampleVal() : drawRainyIfDryRR.sampleVal();
			} else {
				nextRain = (isRaining) ? drawRainyIfRainingNotRR.sampleVal() : drawRainyIfDryNotRR.sampleVal();
			}
			return new Particle(isRainyRegion, nextRain);
		}
		
		public void reweight(boolean obsRaining) {
			weight = (obsRaining == isRaining) ? 1.0 : 0.0;
		}
		
		public double getWeight() {
			return weight;
		}
		
		public boolean isRaining() {
			return isRaining;
		}
		
		public boolean isRainyRegion() {
			return isRainyRegion;
		}
	}
	
	/* Parameters for model */
	private static final int NUM_TIMESTEPS = 6;
	
	private static final double PROB_RR = 0.5;
	
	private static final double PROB_RAINY_INITIAL_RR = 0.7;
	private static final double PROB_RAINY_INITIAL_NOT_RR = 0.3;
	
	private static final double PROB_RAINY_RR_RAINY = 0.8;
	private static final double PROB_RAINY_RR_DRY = 0.3;
	private static final double PROB_RAINY_NOT_RR_RAINY = 0.5;
	private static final double PROB_RAINY_NOT_RR_DRY = 0.2;
	
	private static final boolean[] OBSERVATIONS = {true, true, true, true, false, false};
	
	/* Variables for model */
	private Particle[] modelState;
	
	/* Distributions for sampling in model */
	private final Bernoulli drawRainyRegion;
	private final Bernoulli drawInitialRainyIfRR;
	private final Bernoulli drawInitialRainyIfNotRR;
	
	private final Bernoulli drawRainyIfRainingRR;
	private final Bernoulli drawRainyIfDryRR;
	private final Bernoulli drawRainyIfRainingNotRR;
	private final Bernoulli drawRainyIfDryNotRR;
	
	/* Parameters for inference algorithm */
	private static final int NUM_SAMPLES = 100000;
	
	/* Queries */
	private Map<Boolean, Double> rainyPredictionEstimates;
	private Map<Boolean, Double> rainyRegionEstimates;
	
	public Weather() {
		Util.initRandom(false);		
		modelState = new Weather.Particle[NUM_SAMPLES];
		
		drawRainyRegion = new Bernoulli(PROB_RR);
		drawInitialRainyIfRR = new Bernoulli(PROB_RAINY_INITIAL_RR);
		drawInitialRainyIfNotRR = new Bernoulli(PROB_RAINY_INITIAL_NOT_RR);
		
		drawRainyIfRainingRR = new Bernoulli(PROB_RAINY_RR_RAINY);
		drawRainyIfDryRR = new Bernoulli(PROB_RAINY_RR_DRY);
		drawRainyIfRainingNotRR = new Bernoulli(PROB_RAINY_NOT_RR_RAINY);
		drawRainyIfDryNotRR = new Bernoulli(PROB_RAINY_NOT_RR_DRY);
		
		rainyPredictionEstimates = new HashMap<Boolean, Double>();
		rainyRegionEstimates = new HashMap<Boolean, Double>();
	}
	
	public void runInference() {
		int particleCount = 0;
		while (particleCount < NUM_SAMPLES) {
			Particle p = new Particle();
			p.reweight(OBSERVATIONS[0]);
			
			if (p.getWeight() > 0) {
				modelState[particleCount] = p;
				particleCount++;
			}
		}
		
		for (int t = 1; t <= NUM_TIMESTEPS; t++) {
			Particle[] newPopulation = new Particle[NUM_SAMPLES];
			for (int i = 0; i < NUM_SAMPLES; i++) {
				newPopulation[i] = modelState[i].propagate();
				
				if (t < OBSERVATIONS.length) {
					newPopulation[i].reweight(OBSERVATIONS[t]);
				}
			}
			
			particleCount = 0;
			while (particleCount < NUM_SAMPLES) {
				int particleIndex = Util.randInt(NUM_SAMPLES);
				Particle particleDrawn = newPopulation[particleIndex];
				
				if (Util.random() < particleDrawn.getWeight()) {
					modelState[particleCount] = new Particle(particleDrawn.isRainyRegion(), particleDrawn.isRaining());
					particleCount++;
				}
			}
		}
		
		for (Particle p: modelState) {
			boolean isRR = p.isRainyRegion();
			if (rainyRegionEstimates.containsKey(isRR)) {
				rainyRegionEstimates.put(isRR, rainyRegionEstimates.get(isRR) + 1.0 / NUM_SAMPLES);
			} else {
				rainyRegionEstimates.put(isRR, 1.0 / NUM_SAMPLES);
			}
			
			boolean isRain = p.isRaining();
			if (rainyPredictionEstimates.containsKey(isRain)) {
				rainyPredictionEstimates.put(isRain, rainyPredictionEstimates.get(isRain) + 1.0 / NUM_SAMPLES);
			} else {
				rainyPredictionEstimates.put(isRain, 1.0 / NUM_SAMPLES);
			}
		}
	}
	
	public void printStats() {
		System.out.println("Estimates for time 6:");
		System.out.println("True:\t" + rainyPredictionEstimates.get(true));
		System.out.println("False:\t" + rainyPredictionEstimates.get(false));
		
		System.out.println("\nEstimates for rainy region:");
		System.out.println("True:\t" + rainyRegionEstimates.get(true));
		System.out.println("False:\t" + rainyRegionEstimates.get(false));
	}
	
	public static void main(String[] args) {
		Weather model = new Weather();
		model.runInference();
		model.printStats();
	}
}
