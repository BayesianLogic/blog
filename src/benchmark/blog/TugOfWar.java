package benchmark.blog;

import java.util.HashMap;
import java.util.Map;

import blog.common.Util;
import blog.distrib.Bernoulli;
import blog.distrib.Gaussian;

public class TugOfWar {
	/* Parameters for model */
	private static final double PROB_PLAYER_LAZY = 0.1;
	private static final double MEAN_SKILL = 10;
	private static final double VAR_SKILL = 2;
	
	private static final int NUM_ROUNDS = 3;
	
	/* Distributions for variables in model */
	private final Gaussian drawSkill;
	private final Bernoulli drawLaziness;
	
	/* Person */
	class Person {
		private double skill;
		private boolean[] isLazy;
		
		public Person() {
			skill = drawSkill.sampleVal();
			isLazy = new boolean[NUM_ROUNDS];
			for (int r = 0; r < NUM_ROUNDS; r++) {
				isLazy[r] = drawLaziness.sampleVal();
			}
		}
		
		public double getSkill() {
			return skill;
		}
		
		public double getStrength(int roundNum) {
			return (isLazy[roundNum]) ? skill / 2 : skill;
		}
	}
	
	/* Variables in model */
	private Person james, david, george, ronald, john, richard, charles, jeff,
		kevin, anthony, robert, paul;
	
	/* Parameters for inference algorithm */
	private static final int NUM_SAMPLES = 500000;
	private static int SAMPLES_ACCEPTED = 0;
	
	/* Queries */
	private final Map<Boolean, Double> winRoundTwo;
	private final Map<Boolean, Double> winRoundThree;
	private final Map<Double, Double> skillOfJames;
	
	public TugOfWar() {
		Util.initRandom(false);
		
		drawSkill = new Gaussian(MEAN_SKILL, VAR_SKILL);
		drawLaziness = new Bernoulli(PROB_PLAYER_LAZY);
		
		winRoundTwo = new HashMap<Boolean, Double>();
		winRoundThree = new HashMap<Boolean, Double>();
		skillOfJames = new HashMap<Double, Double>();
	}
	
	public void runInference() {		
		for (int i = 0; i < NUM_SAMPLES; i++) {
			regeneratePlayers();
			if (firstTeamScore(0) < secondTeamScore(0)) {
				continue;
			}
			SAMPLES_ACCEPTED++;
			
			boolean roundTwoResult = firstTeamScore(1) > secondTeamScore(1);
			if (winRoundTwo.containsKey(roundTwoResult)) {
				winRoundTwo.put(roundTwoResult, winRoundTwo.get(roundTwoResult) + 1);
			} else {
				winRoundTwo.put(roundTwoResult, 1.0);
			}
			
			boolean roundThreeResult = firstTeamScore(2) > secondTeamScore(2);
			if (winRoundThree.containsKey(roundThreeResult)) {
				winRoundThree.put(roundThreeResult, winRoundThree.get(roundThreeResult) + 1);
			} else {
				winRoundThree.put(roundThreeResult, 1.0);
			}
			
			if (skillOfJames.containsKey(james.getSkill())) {
				skillOfJames.put(james.getSkill(), skillOfJames.get(james.getSkill()) + 1);
			} else {
				skillOfJames.put(james.getSkill(), 1.0);
			}
		}
		
		winRoundTwo.put(true, winRoundTwo.get(true) / SAMPLES_ACCEPTED);
		winRoundTwo.put(false, winRoundTwo.get(false) / SAMPLES_ACCEPTED);
		
		winRoundThree.put(true, winRoundThree.get(true) / SAMPLES_ACCEPTED);
		winRoundThree.put(false, winRoundThree.get(false) / SAMPLES_ACCEPTED);
		
		for (double skill: skillOfJames.keySet()) {
			skillOfJames.put(skill, skillOfJames.get(skill) / SAMPLES_ACCEPTED);
		}
	}
	
	private void regeneratePlayers() {
		james = new Person();
		david = new Person();
		george = new Person();
		ronald = new Person();
		john = new Person();
		richard = new Person();
		charles = new Person();
		jeff = new Person();
		kevin = new Person();
		anthony = new Person();
		robert = new Person();
		paul = new Person();
	}
	
	private double firstTeamScore(int round) {
		if (round == 0) {
			return james.getStrength(0) + david.getStrength(0) + george.getStrength(0);
		} else if (round == 1) {
			return charles.getStrength(1) + david.getStrength(1) + jeff.getStrength(1);
		} else if (round == 2) {
			return kevin.getStrength(2) + robert.getStrength(2) + jeff.getStrength(2);
		} else {
			return 0.0;
		}
	}
	
	private double secondTeamScore(int round) {
		if (round == 0) {
			return ronald.getStrength(0) + john.getStrength(0) + richard.getStrength(0);
		} else if (round == 1) {
			return ronald.getStrength(1) + kevin.getStrength(1) + anthony.getStrength(1);
		} else if (round == 2) {
			return paul.getStrength(2) + charles.getStrength(2) + anthony.getStrength(2);
		} else {
			return 0.0;
		}
	}
	
	public void printStats() {
		System.out.println("Number of samples: " + NUM_SAMPLES);
		System.out.println("Number of samples accepted: " + SAMPLES_ACCEPTED);
		System.out.println("\nDid Team 1 win Round 2?");
		System.out.println("True:\t" + winRoundTwo.get(true));
		System.out.println("False:\t" + winRoundTwo.get(false));
		
		System.out.println("\nDid Team 1 win Round 3?");
		System.out.println("True:\t" + winRoundThree.get(true));
		System.out.println("False:\t" + winRoundThree.get(false));
		
		System.out.println("\nJames's skill:");
		
		double meanSkillJames = 0;
		for (double skillSample: skillOfJames.keySet()) {
			meanSkillJames += skillSample * skillOfJames.get(skillSample);
		}
		System.out.println("Mean:\t" + meanSkillJames);
		
		double varSkillJames = 0;
		for (double skillSample: skillOfJames.keySet()) {
			varSkillJames += Math.pow(skillSample - meanSkillJames, 2.0);
		}
		varSkillJames /= SAMPLES_ACCEPTED;
		System.out.println("Variance:\t" + varSkillJames);
	}
	
	public static void main(String[] args) {
		TugOfWar model = new TugOfWar();
		model.runInference();
		model.printStats();
	}
}
