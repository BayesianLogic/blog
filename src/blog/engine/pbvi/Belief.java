package blog.engine.pbvi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.BuiltInTypes;
import blog.model.Evidence;
import blog.model.Function;
import blog.model.Term;
import blog.model.Type;
import blog.world.AbstractPartialWorld;

public class Belief {
	private PFEngineSampled pf;
	private OUPOMDPModel pomdp;
	private Map<State, Integer> stateCounts;
	private Evidence latestEvidence;
	private Double latestReward;
	
	public Belief(OUPOMDPModel pomdp) {
		this.stateCounts = new HashMap<State, Integer>();
		this.pomdp = pomdp;
	}
	
	public Belief(Map<State, Integer> states, OUPOMDPModel pomdp) {
		this.stateCounts = new HashMap<State, Integer>();
		for (State s : states.keySet()) {
			this.stateCounts.put(s, states.get(s)); 
		}
		this.pomdp = pomdp;
		//zeroTimestep();
	}
	
	public Belief(PFEngineSampled pf, OUPOMDPModel pomdp) {
		this.pf = pf;
		this.pomdp = pomdp;
		
		stateCounts = new HashMap<State, Integer>();
		for (TimedParticle tp : pf.particles) {
			State s = new State((AbstractPartialWorld) tp.curWorld, tp.getTimestep());
			addState(s);
		}
	}
	
	public int getTimestep() {
		return ((State) Util.getFirst(getStates())).getTimestep();
	}
	
	public void addState(State s) {
		addState(s, 1);
	}
	
	public void addState(State s, Integer count) {
		if (!stateCounts.containsKey(s)) {
			stateCounts.put(s, 0);
		}
		stateCounts.put(s, stateCounts.get(s) + count);
	}
	
	public void addBelief(Belief b) {
		for (State s : b.getStates()) {
			addState(s, b.getCount(s));
		}
	}
	
	static Map<Integer, Integer> resampleStateCountStats = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> stateCountStats = new HashMap<Integer, Integer>();
	public Belief sampleNextBelief(Evidence action) {
		action = translateAction(action);
		Timer.start("BELIEF_PROP");
		PFEngineSampled nextPF = getParticleFilter().copy();
		
		Timer.start("takeAction");
		nextPF.beforeTakingEvidence();
		nextPF.takeDecision(action);
		nextPF.answer(pomdp.getQueries(getTimestep() + 1));
		Timer.record("takeAction");
		
		double reward = getAvgReward(nextPF);
		
		for (TimedParticle p : nextPF.particles)
			p.advanceTimestep();

		Timer.start("updateOS");
		nextPF.updateOSforAllParticles();
		Timer.record("updateOS");
		
		int osIndex = nextPF.particles.get(0).getOS();
		nextPF.retakeObservability2();
		osIndex = nextPF.retakeObservability();	
		Evidence o = ObservabilitySignature.getOSbyIndex(osIndex).getEvidence();
		
		if (UBT.dropHistory) {
			nextPF.dropHistory();
			//ObservabilitySignature.dropHistory(((TimedParticle)Util.getFirst(nextPF.particles)).getTimestep());
		}
		//takeObsTime += Timer.getElapsed();
		
		if (nextPF.particles.size() > 1) {
			
			nextPF.resample();
			Timer.record("resample");
		}
		
		Belief nextBelief = new Belief(nextPF, pomdp);
		nextBelief.latestReward = reward;
		nextBelief.latestEvidence = o;
		
		Timer.record("BELIEF_PROP");
		updateResampleStateCountStats(nextBelief);
		updateStateCountStats(this);
		return nextBelief;
	}
	
	public double getReward(Evidence action) {
		action = translateAction(action);
		PFEngineSampled apPF = getParticleFilter().copy();
		apPF.beforeTakingEvidence();
		apPF.takeDecision(action);
		apPF.answer(pomdp.getQueries(getTimestep() + 1));
		
		return getAvgReward(apPF);
	}

	/**
	 * Should be called after applying action but before advancing particles' timestep
	 * @param pf
	 * @return
	 */
	private double getAvgReward(PFEngineSampled pf) {
		double total = 0;
		Function rewardFunc = (Function) pomdp.getModel().getRandomFunc("reward", 1);
		Object timestep = Type.getType("Timestep").getGuaranteedObject(pf.particles.get(0).getTimestep());
		//System.out.println("Get reward: " + timestep);
		for (TimedParticle p : pf.particles) {
			Number reward = (Number) rewardFunc.getValueSingleArg(timestep, p.getLatestWorld());
			if (reward == null) {
				System.out.println("Reward is null at " + timestep);
				System.out.println(p.getLatestWorld());
			}
			total += reward.doubleValue();
		}
		return total/pf.particles.size();
	}
	
	public ActionPropagated beliefsAfterAction(Evidence action) {
		action = translateAction(action);
		
		updateStateCountStats(this);
		Timer.start("BELIEF_PROP");
		ActionPropagated ap = new ActionPropagated(this, action);
		
		PFEngineSampled apPF = getParticleFilter().copy();
		apPF.beforeTakingEvidence();
		apPF.takeDecision(action);
		apPF.answer(pomdp.getQueries(getTimestep() + 1));
		
		Function rewardFunc = (Function) pomdp.getModel().getRandomFunc("reward", 1);
		Object timestep = Type.getType("Timestep").getGuaranteedObject(getTimestep());
	
		Number reward = (Number) rewardFunc.getValueSingleArg(timestep, 
				apPF.particles.get(0).getLatestWorld());
		ap.setReward(reward.doubleValue());
		
		for (TimedParticle p : apPF.particles)
			p.advanceTimestep();
		apPF.updateOSforAllParticles();
		
		Map<Integer, Double> osWeights = new HashMap<Integer, Double>();
		for (TimedParticle p : apPF.particles) {
			Integer os = p.getOS();
			if (!osWeights.containsKey(os))
				osWeights.put(os, 0.0);
			osWeights.put(os, osWeights.get(os) + p.getLatestWeight());
		}
		//System.out.println("Num observations " + osWeights.size());
		ap.setActionPropagatedPF(apPF);
		for (Integer osIndex : osWeights.keySet()) {
			PFEngineSampled nextPF = apPF.copy();
			nextPF.retakeObservability2(osIndex);
			nextPF.retakeObservability(osIndex);	
			
			//if (UBT.dropHistory) {
				//nextPF.dropHistory();
				//ObservabilitySignature.dropHistory(((TimedParticle)Util.getFirst(nextPF.particles)).getTimestep());
			//}
			//nextPF.resample();
			/*Belief nextBelief = new Belief(nextPF, pbvi);
			result.put(o, nextBelief);
			ap.setNextBelief(o, nextBelief);*/
			Evidence o = ObservabilitySignature.getOSbyIndex(osIndex).getEvidence();
			ap.setObservationWeight(o, osWeights.get(osIndex));
			ap.setOSIndex(o, osIndex);
		}
		Timer.record("BELIEF_PROP");
		return ap;
	}
	
	private Evidence translateAction(Evidence action) {
		int actionTimestep = DBLOGUtil.getTimestepIndex(Util.getFirst(action.getEvidenceVars()));
		Term toReplace = BuiltInTypes.TIMESTEP.getCanonicalTerm(
				BuiltInTypes.TIMESTEP.getGuaranteedObject(actionTimestep));
		Term replacement = BuiltInTypes.TIMESTEP.getCanonicalTerm(BuiltInTypes.TIMESTEP.getGuaranteedObject(this.getTimestep()));
		
		if (actionTimestep == this.getTimestep()) return action;	
		//System.out.println(actionTimestep + " " + this.getTimestep());
		//System.out.println("action" + action);
		Evidence newAction = action.replace(toReplace, replacement);
		//System.out.println("replace " + action + " replacement" + newAction);	
		return newAction;
	}

	public PFEngineSampled getParticleFilter() {
		if (pf != null)
			return pf;
		
		int count = 0;
		for (State s : getStates()) {
			count += stateCounts.get(s);
		}
		
		Properties properties = (Properties) pomdp.getProperties().clone();
		properties.setProperty("numParticles", "" + count);
		pf = new PFEngineSampled(pomdp.getModel(), properties);
		List<TimedParticle> particles = pf.particles;
		int j = 0;
		for (State s : getStates()) {
			for (int i = 0; i < stateCounts.get(s); i++) {
				particles.get(j).setWorld(s.getWorld());
				particles.get(j).setTimestep(s.getTimestep());
				j++;
			}
		}
		return pf;
	}
	
	public Set<State> getStates() {
		return stateCounts.keySet();
	}
	
	public Integer getCount(State s) {
		Integer count = stateCounts.get(s);
		if (count == null) 
			return 0;
		return count;
	}
	
	public String toString() {
		Set<State> states = getStates();
		String result = "";
		for (State s : states) {
			result += getCount(s) + " " + s.getWorld() + "\n";
		}
		return result;
	}

	public void setParticleFilter(PFEngineSampled particleFilter) {
		this.pf = particleFilter;
	}

	public void setPBVI(OUPOMDPModel pomdp) {
		this.pomdp = pomdp;
	}
	
	public Evidence getLatestEvidence() {
		//System.out.println("getLatestEvidence " + latestEvidence);
		//Evidence translated = translateAction(latestEvidence);
		//System.out.println("translated " + translated);
		//return translated;
		return latestEvidence;
	}
	
	public int diffNorm(Belief other) {
		Set<State> unionStates = new HashSet<State>(this.stateCounts.keySet());
		unionStates.addAll(other.getStates());
		int diff = 0;
		for (State s : unionStates) {
			diff += Math.abs(this.getCount(s) - other.getCount(s));
		}
		return diff;
	}
	
	private void zeroTimestep() {
		Set<State> states = getStates();
		Map<State, Integer> newStateCounts = new HashMap<State, Integer>();
		for (State s : states) {
			int count = stateCounts.get(s);
			s.zeroTimestep(0);
			newStateCounts.put(s, count);
		}
		stateCounts = newStateCounts;
		pf = null;
	}
	
	public boolean ended() {
		Function endStateFunc = (Function) pomdp.getModel().getRandomFunc("end_state", 1);
		Object timestep = Type.getType("Timestep").getGuaranteedObject(getTimestep());
		Boolean ended = 
				(Boolean) endStateFunc.getValueSingleArg(timestep, getParticleFilter().particles.get(0).curWorld);
		if (ended == null) { 
			System.out.println("Why is ended null?" + timestep);
			System.out.println(this);
			return false;
		}
		return ended;
	}
	
	public double getLatestReward() {
		return latestReward;
	}
	
	public static void printTimingStats() {
		System.out.println("Belief.resampleTime " + Timer.niceTimeString(Timer.getAggregate("resample")));
		System.out.println("Belief.copyTime " + Timer.niceTimeString(Timer.getAggregate("copy")));
		System.out.println("Belief.takeActionTime " + Timer.niceTimeString(Timer.getAggregate("takeAction")));
		System.out.println("Belief.takeObsTime " + Timer.niceTimeString(Timer.getAggregate("takeObs")));
		
		System.out.println("State counts " + stateCountStats);
		System.out.println("Resample state counts " + resampleStateCountStats);
	}

	public static void updateResampleStateCountStats(Belief nextBelief) {
		int numStatesAfterResample = nextBelief.getStates().size();
		if (!resampleStateCountStats.containsKey(numStatesAfterResample)) {
			resampleStateCountStats.put(numStatesAfterResample, 0);
		}
		resampleStateCountStats.put(numStatesAfterResample, resampleStateCountStats.get(numStatesAfterResample) + 1);
	}
	
	public static void updateStateCountStats(Belief belief) {
		int numStates = belief.getStates().size();
		if (!stateCountStats.containsKey(numStates)) {
			stateCountStats.put(numStates, 0);
		}
		stateCountStats.put(numStates, stateCountStats.get(numStates) + 1);
	}
	

	public static Belief getSingletonBelief(State state, int numParticles, OUPOMDPModel pomdp) {
		Properties properties = (Properties) pomdp.getProperties().clone();
		properties.setProperty("numParticles", "" + numParticles);
		PFEngineSampled pf = new PFEngineSampled(pomdp.getModel(), properties, state.getWorld(), state.getTimestep());
		Belief b = new Belief(pf, pomdp);
		return b;
	}
}
