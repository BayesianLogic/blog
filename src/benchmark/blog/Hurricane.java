package benchmark.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Categorical;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.FuncAppTerm;
import blog.model.NonRandomFunction;
import blog.model.SymbolTerm;
import blog.model.Term;

/**
 * Native implementation of hurricane inference algorithm.
 * Used for a performance comparison with BLOG.
 * 
 * @author awong
 */
public class Hurricane {
	// Parameters for model
	private static final double PROB_FIRST_A = 0.5;
	private static final double PROB_PREP_IF_FIRST = 0.5;
	private static final double PROB_PREP_IF_MILD = 0.1;
	private static final double PROB_PREP_IF_SEVERE = 0.9;
	private static final double PROB_DAMAGE_IF_HIGH = 0.2;
	private static final double PROB_DAMAGE_IF_LOW = 0.8;
	
	public static final String PREP_LOW = "Low";
	public static final String PREP_HIGH = "High";
	public static final String DAMAGE_LOW = "Mild";
	public static final String DAMAGE_HIGH = "Severe";
	
	public static final String CITY_A = "A";
	public static final String CITY_B = "B";
	
	// Parameters for inference algorithm
	private static final int NUM_SAMPLES = 2500000;
	
	// Variables for model
	private String cityFirst;
	private String prepA;
	private String damageA;
	private String prepB;
	private String damageB;
	
	// Distributions for model
	private Categorical drawFirst;
	private Categorical drawPrepFirst;
	private Categorical drawPrepSecondDamageLow;
	private Categorical drawPrepSecondDamageHigh;
	private Categorical drawDamagePrepLow;
	private Categorical drawDamagePrepHigh;
	
	// Queries
	private double thisWeight;
	private double totalWeight;
	private Map<String, Double> statFirst;
	private Map<String, Double> statDamageA;
	private Map<String, Double> statDamageB;
	private Map<String, Double> statDamageSecond;
	
	public Hurricane() {
		Util.initRandom(false);
		
		/* Construct query stuff */
		statFirst = new HashMap<String, Double>();
		statDamageA = new HashMap<String, Double>();
		statDamageB = new HashMap<String, Double>();
		statDamageSecond = new HashMap<String, Double>();
		
		/* Construct categorical distribution for first city */
		String[] cities = {CITY_A, CITY_B};
		double[] firstProb = {PROB_FIRST_A, 1 - PROB_FIRST_A};
		drawFirst = createCategorical(cities, firstProb);
		
		/* Construct categorical distribution for first city's prep */
		String[] prepLevels = {PREP_HIGH, PREP_LOW};
		double[] prepFirstProb = {PROB_PREP_IF_FIRST, 1 - PROB_PREP_IF_FIRST};
		drawPrepFirst = createCategorical(prepLevels, prepFirstProb);

		/* Construct categorical distributions for second city's prep */
		double[] prepSecondLowProb = {PROB_PREP_IF_MILD, 1 - PROB_PREP_IF_MILD};
		drawPrepSecondDamageLow = createCategorical(prepLevels, prepSecondLowProb);
		
		double[] prepSecondHighProb = {PROB_PREP_IF_SEVERE, 1 - PROB_PREP_IF_SEVERE};
		drawPrepSecondDamageHigh = createCategorical(prepLevels, prepSecondHighProb);
		
		/* Construct categorical distributions for city damage */
		String[] damageLevels = {DAMAGE_HIGH, DAMAGE_LOW};
		double[] damageProbPrepLow = {PROB_DAMAGE_IF_LOW, 1 - PROB_DAMAGE_IF_LOW};
		drawDamagePrepLow = createCategorical(damageLevels, damageProbPrepLow);
		
		double[] damageProbPrepHigh = {PROB_DAMAGE_IF_HIGH, 1 - PROB_DAMAGE_IF_HIGH};
		drawDamagePrepHigh = createCategorical(damageLevels, damageProbPrepHigh);
	}
	
	private Categorical createCategorical(String[] terms, double[] probs) {
		Map<ArgSpec, Term> argsToProbs = new HashMap<ArgSpec, Term>();
		argsToProbs.put(new FuncAppTerm(NonRandomFunction.createConstant(terms[0], BuiltInTypes.STRING, terms[0])),
						new FuncAppTerm(NonRandomFunction.createConstant("" + probs[0], BuiltInTypes.REAL, probs[0])));
		argsToProbs.put(new FuncAppTerm(NonRandomFunction.createConstant(terms[1], BuiltInTypes.STRING, terms[1])),
						new FuncAppTerm(NonRandomFunction.createConstant("" + probs[1], BuiltInTypes.REAL, probs[1])));
		List<Map<ArgSpec, Term>> argProbList = new ArrayList<Map<ArgSpec, Term>>();
		argProbList.add(argsToProbs);
		return new Categorical(argProbList);
	}
	
	public void runInference(int num_samples) {
		List<Object> empty = new ArrayList();
		
		for (int i = 0; i < num_samples; i++) {
			cityFirst = (String) drawFirst.sampleVal(empty, null);
			if (cityFirst == CITY_A) {
				prepA = (String) drawPrepFirst.sampleVal(empty, null);
				damageA = DAMAGE_HIGH;
				thisWeight = (prepA == PREP_HIGH) ?
						drawDamagePrepHigh.getProb(empty, damageA) : drawDamagePrepLow.getProb(empty, damageA);
				
				prepB = (String) drawPrepSecondDamageHigh.sampleVal(empty, null);
				damageB = (String) ((prepB == PREP_HIGH) ? 
						drawDamagePrepHigh.sampleVal(empty, null) : drawDamagePrepLow.sampleVal(empty, null));
			} else {
				prepB = (String) drawPrepFirst.sampleVal(empty, null);
				damageB = DAMAGE_HIGH;
				thisWeight = (prepB == PREP_HIGH) ?
						drawDamagePrepHigh.getProb(empty, damageB) : drawDamagePrepLow.getProb(empty, damageB);
				
				prepA = (String) ((damageB == DAMAGE_HIGH) ? 
						drawPrepSecondDamageHigh.sampleVal(empty, null) : drawPrepSecondDamageLow.sampleVal(empty, null));
				damageA = (String) ((prepA == PREP_HIGH) ? 
						drawDamagePrepHigh.sampleVal(empty, null) : drawDamagePrepLow.sampleVal(empty, null));
			}
			
			totalWeight += thisWeight;
			statFirst.put(cityFirst, statFirst.containsKey(cityFirst) ? statFirst.get(cityFirst) + thisWeight : thisWeight);
			statDamageA.put(damageA, statDamageA.containsKey(damageA) ? statDamageA.get(damageA) + thisWeight : thisWeight);
			statDamageB.put(damageB, statDamageB.containsKey(damageB) ? statDamageA.get(damageB) + thisWeight : thisWeight);
			
			if (cityFirst == CITY_A) {
				statDamageSecond.put(damageB, statDamageSecond.containsKey(damageB) ?
						statDamageSecond.get(damageB) + thisWeight : thisWeight);
			} else {
				statDamageSecond.put(damageA, statDamageSecond.containsKey(damageA) ?
						statDamageSecond.get(damageA) + thisWeight : thisWeight);
			}
		}
	}
	
	public void printStats() {
		System.out.println("Total samples: " + NUM_SAMPLES);
		System.out.println("Average world weight: " + totalWeight / NUM_SAMPLES);
		
		System.out.println("\nFirst city:");
		for (String city : statFirst.keySet()) {
			System.out.println(city + ":\t" + statFirst.get(city) / totalWeight);
		}
		
		System.out.println("\nDamage to city A:");
		for (String city : statDamageA.keySet()) {
			System.out.println(city + ":\t" + statDamageA.get(city) / totalWeight);
		}
		
		System.out.println("\nDamage to city B:");
		for (String city : statDamageB.keySet()) {
			System.out.println(city + ":\t" + statDamageB.get(city) / totalWeight);
		}
		
		System.out.println("\nDamage to second city hit:");
		for (String city : statDamageSecond.keySet()) {
			System.out.println(city + ":\t" + statDamageSecond.get(city) / totalWeight);
		}
	}
	
	public static void main(String[] args) {
		Hurricane model = new Hurricane();
		model.runInference(NUM_SAMPLES);
		model.printStats();
	}
}
