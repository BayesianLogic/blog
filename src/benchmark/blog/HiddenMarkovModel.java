package benchmark.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Categorical;
import blog.distrib.TabularCPD;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.FuncAppTerm;
import blog.model.NonRandomFunction;
import blog.model.Term;

public class HiddenMarkovModel {
	/* Parameters for model */
	private static final int NUM_TIMESTEPS = 5;
	
	private static final String STATE_A = "A";
	private static final String STATE_C = "C";
	private static final String STATE_G = "G";
	private static final String STATE_T = "T";
	
	private static final double PROB_INITIAL_A = 0.3;
	private static final double PROB_INITIAL_C = 0.2;
	private static final double PROB_INITIAL_G = 0.1;
	private static final double PROB_INITIAL_T = 0.4;
	
	private static final double PROB_STATE_SAME = 0.1;
	private static final double PROB_STATE_CHANGE = 0.3;
	private static final double PROB_OBS_TRUTH = 0.85;
	private static final double PROB_OBS_ERROR = 0.05;
	
	private static final String[] OBSERVATIONS = {STATE_C, STATE_A, STATE_A, STATE_A, STATE_G};
	
	/* Variables for model */
	private String[] prevStates;
	private String[] currentStates;
	
	/* Distributions for sampling in model */
	private Categorical drawInitialState;
	private Map<String, Categorical> drawNextState;
	private Map<String, Categorical> drawNextObs;
	
	/* Parameters for inference algorithm */
	private static final int NUM_SAMPLES = 50000;
	
	/* Queries */
	private Map<String, Double>[] stateEstimates;
	
	public HiddenMarkovModel() {
		Util.initRandom(false);
		
		/* Init distributions: initial */
		String[] legalStates = {STATE_A, STATE_C, STATE_G, STATE_T};
		double[] initStateProbs = {PROB_INITIAL_A, PROB_INITIAL_C, PROB_INITIAL_G, PROB_INITIAL_T};
		drawInitialState = createCategorical(legalStates, initStateProbs);
		
		/* Init distributions: next state */
		drawNextState = new HashMap<String, Categorical>();
		
		double[] continueStateProbsA = {PROB_STATE_SAME, PROB_STATE_CHANGE, PROB_STATE_CHANGE, PROB_STATE_CHANGE};
		drawNextState.put(STATE_A, createCategorical(legalStates, continueStateProbsA));
		
		double[] continueStateProbsC = {PROB_STATE_CHANGE, PROB_STATE_SAME, PROB_STATE_CHANGE, PROB_STATE_CHANGE};
		drawNextState.put(STATE_C, createCategorical(legalStates, continueStateProbsC));
		
		double[] continueStateProbsG = {PROB_STATE_CHANGE, PROB_STATE_CHANGE, PROB_STATE_SAME, PROB_STATE_CHANGE};
		drawNextState.put(STATE_G, createCategorical(legalStates, continueStateProbsG));
		
		double[] continueStateProbsT = {PROB_STATE_CHANGE, PROB_STATE_CHANGE, PROB_STATE_CHANGE, PROB_STATE_SAME};
		drawNextState.put(STATE_T, createCategorical(legalStates, continueStateProbsT));
		
		/* Init distributions: next obs */
		String[] legalObs = {STATE_A, STATE_C, STATE_G, STATE_T};
		drawNextObs = new HashMap<String, Categorical>();
		
		double[] obsProbsA = {PROB_OBS_TRUTH, PROB_OBS_ERROR, PROB_OBS_ERROR, PROB_OBS_ERROR};
		drawNextObs.put(STATE_A, createCategorical(legalObs, obsProbsA));
		
		double[] obsProbsC = {PROB_OBS_ERROR, PROB_OBS_TRUTH, PROB_OBS_ERROR, PROB_OBS_ERROR};
		drawNextObs.put(STATE_C, createCategorical(legalObs, obsProbsC));
		
		double[] obsProbsG = {PROB_OBS_ERROR, PROB_OBS_ERROR, PROB_OBS_TRUTH, PROB_OBS_ERROR};
		drawNextObs.put(STATE_G, createCategorical(legalObs, obsProbsG));
		
		double[] obsProbsT = {PROB_OBS_ERROR, PROB_OBS_ERROR, PROB_OBS_ERROR, PROB_OBS_TRUTH};
		drawNextObs.put(STATE_T, createCategorical(legalObs, obsProbsT));
		
		/* Init query stuff */
		stateEstimates = (Map<String, Double>[]) new Map[NUM_TIMESTEPS + 1];
		for (int i = 0; i <= NUM_TIMESTEPS; i++) {
			stateEstimates[i] = new HashMap<String, Double>();
		}
	}
	
	private Categorical createCategorical(String[] terms, double[] probs) {
		Map<ArgSpec, Term> argsToProbs = new HashMap<ArgSpec, Term>();
		for (int i = 0; i < terms.length; i++) {
			argsToProbs.put(new FuncAppTerm(NonRandomFunction.createConstant(terms[i], BuiltInTypes.STRING, terms[i])),
							new FuncAppTerm(NonRandomFunction.createConstant("" + probs[i], BuiltInTypes.REAL, probs[i])));
		}
		List<Map<ArgSpec, Term>> argProbList = new ArrayList<Map<ArgSpec, Term>>();
		argProbList.add(argsToProbs);
		return new Categorical(argProbList);
	}
	
	public void runInference() {
		List<String> empty = new ArrayList<String>();
		
		for (int t = 0; t <= NUM_TIMESTEPS; t++) {
			Map<String, Double> particleWeights = new HashMap<String, Double>();
			prevStates = currentStates;
			
			// Propagate particles
			for (int i = 0; i < NUM_SAMPLES; i++) {
				String stateDrawn;
				if (t == 0) {
					stateDrawn = (String) drawInitialState.sampleVal(empty, BuiltInTypes.STRING);	
				} else {
					stateDrawn = (String) drawNextState.get(prevStates[i]).sampleVal(empty, BuiltInTypes.STRING);
				}
				
				if (particleWeights.containsKey(stateDrawn)) {
					particleWeights.put(stateDrawn, particleWeights.get(stateDrawn) + 1);
				} else {
					particleWeights.put(stateDrawn, 1.0);
				}
			}
			
			// Generate observations, reweight particles
			double totalWeight = 0.0;
			if (t < OBSERVATIONS.length) {
				for (String stateValue: particleWeights.keySet()) {
					double stateWeight;
					if (stateValue.equals(OBSERVATIONS[t])) {
						stateWeight = particleWeights.get(stateValue) * PROB_OBS_TRUTH;
					} else {
						stateWeight = particleWeights.get(stateValue) * PROB_OBS_ERROR;
					}
					totalWeight += stateWeight;
					particleWeights.put(stateValue, stateWeight);
				}
			} else {
				totalWeight = NUM_SAMPLES;
			}
			for (String stateValue: particleWeights.keySet()) {
				particleWeights.put(stateValue, particleWeights.get(stateValue) / totalWeight);
			}
			
			// Create a new distribution to sample from using reweighted particles
			String[] states = particleWeights.keySet().toArray(new String[0]);
			double[] probs = new double[particleWeights.size()];
			for (int i = 0; i < particleWeights.size(); i++) {
				probs[i] = particleWeights.get(states[i]);
			}
			Categorical resampleDistrib = createCategorical(states, probs);
			
			// Resample particles, and count states represented by each particle
			currentStates = new String[NUM_SAMPLES];
			for (int i = 0; i < NUM_SAMPLES; i++) {
				currentStates[i] = (String) resampleDistrib.sampleVal(empty, BuiltInTypes.STRING);
				stateEstimates[t].put(currentStates[i],
						(stateEstimates[t].containsKey(currentStates[i])) ?	stateEstimates[t].get(currentStates[i]) + 1 : 1);
			}
		}
	}
	
	public void printStats() {
		System.out.println("State estimates for model:");
		for (int i = 0; i < stateEstimates.length; i++) {
			System.out.println("Time " + i + ":");
			Map<String, Double> stateForTimestep = stateEstimates[i];
			for (String stateVal: stateForTimestep.keySet()) {
				System.out.println(stateVal + ":\t" + stateForTimestep.get(stateVal) / NUM_SAMPLES);
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		HiddenMarkovModel model = new HiddenMarkovModel();
		model.runInference();
		model.printStats();
	}

}
