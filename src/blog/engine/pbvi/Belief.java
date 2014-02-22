package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;
import blog.common.Util;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.PFEngine.PFEngineSampled;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.world.AbstractPartialWorld;

public class Belief {
	private PFEngineSampled pf;
	private OUPBVI pbvi;
	private Map<State, Integer> states;
	
	public Belief() {
		this.states = new HashMap<State, Integer>();
	}
	
	public Belief(Map<State, Integer> states) {
		this.states = new HashMap<State, Integer>();
		for (State s : states.keySet()) {
			this.states.put(s, states.get(s)); 
		}
	}
	
	public Belief(PFEngineSampled pf, OUPBVI pbvi) {
		this.pf = pf;
		this.pbvi = pbvi;
		
		states = new HashMap<State, Integer>();
		for (TimedParticle tp : pf.particles) {
			State s = new State((AbstractPartialWorld) tp.curWorld, tp.getTimestep());
			addState(s);
		}
	}
	
	public int getTimestep() {
		return ((State) Util.getFirst(getStates().keySet())).getTimestep();
	}
	
	public void addState(State s) {
		addState(s, 1);
	}
	
	public void addState(State s, Integer count) {
		if (!states.containsKey(s)) {
			states.put(s, 0);
		}
		states.put(s, states.get(s) + count);
	}
	
	public void addStates(Map<State, Integer> states) {
		for (State s : states.keySet()) {
			addState(s, states.get(s));
		}
	}
	
	public Belief takeAction(Evidence action) {
		PFEngineSampled actionPropagated = pf.copy();
		actionPropagated.beforeTakingEvidence();
		actionPropagated.takeDecision(action);
		for (TimedParticle p : actionPropagated.particles)
			p.advanceTimestep();
		actionPropagated.answer(pbvi.getQueries(getTimestep() + 1));
		return new Belief(actionPropagated, pbvi);
	}
	
/*	public Belief takeObservation(Evidence obs) {
		PFEngineSampled next = pf.copy();
		next.beforeTakingEvidence();
		next.takeDecision(obs);
		next.answer(pbvi.getQueries(getTimestep()));
		try {
			next.resample();
		} catch (IllegalArgumentException e) {
			return null;
		}
		return new Belief(next, pbvi);
	}*/
	
	public ActionPropagated beliefsAfterAction(Evidence action) {
		Map<Evidence, Belief> result = new HashMap<Evidence, Belief>();
		ActionPropagated ap = new ActionPropagated(this, action);
		
		PFEngineSampled actionPropagated = pf.copy();
		actionPropagated.beforeTakingEvidence();
		actionPropagated.takeDecision(action);
		actionPropagated.answer(pbvi.getQueries(getTimestep() + 1));
		
		for (TimedParticle p : actionPropagated.particles)
			p.advanceTimestep();
		actionPropagated.updateOSforAllParticles();
		
		Map<Integer, Double> osWeights = new HashMap<Integer, Double>();
		for (TimedParticle p : actionPropagated.particles) {
			Integer os = p.getOS();
			if (!osWeights.containsKey(os))
				osWeights.put(os, 0.0);
			osWeights.put(os, osWeights.get(os) + p.getLatestWeight());
		}
		//System.out.println("Num observations " + osWeights.size());
		
		for (Integer osIndex : osWeights.keySet()) {
			PFEngineSampled nextPF = actionPropagated.copy();
			nextPF.retakeObservability2(osIndex);
			nextPF.retakeObservability(osIndex);	
			Evidence o = ObservabilitySignature.getOSbyIndex(osIndex).getEvidence();
			if (UBT.dropHistory) {
				nextPF.dropHistory();
				//ObservabilitySignature.dropHistory(((TimedParticle)Util.getFirst(nextPF.particles)).getTimestep());
			}
			nextPF.resample();
			Belief nextBelief = new Belief(nextPF, pbvi);
			result.put(o, nextBelief);
			ap.setNextBelief(o, nextBelief);
			ap.setObservationWeight(o, osWeights.get(osIndex));
		}
		return ap;
	}
	
	public PFEngineSampled getParticleFilter() {
		return pf;
	}
	
	public Map<State, Integer> getStates() {
		return states;
	}
	
	public String toString() {
		Map<State, Integer> counts = getStates();
		String result = "";
		for (State s : counts.keySet()) {
			result += counts.get(s) + " " + s.getWorld() + "\n";
		}
		return result;
	}

	public void setParticleFilter(PFEngineSampled particleFilter) {
		this.pf = particleFilter;
	}

	public void setPBVI(OUPBVI oupbvi) {
		this.pbvi = oupbvi;
	}
}
