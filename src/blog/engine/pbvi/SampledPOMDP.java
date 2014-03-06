package blog.engine.pbvi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.common.Util;
import blog.model.Evidence;

public class SampledPOMDP {
	private Map<State, State> states;
	private Map<Evidence, Evidence> actions;
	private Map<Evidence, Evidence> observations;
	private Map<State, Set<Evidence>> stateActions;
	private Map<Pair<State, Evidence>, Map<Evidence, Integer>> obsWeights;
	private Map<Triplet<State, Evidence, Evidence>, Belief> nextBeliefs;
	private Map<Pair<State, Evidence>, Double> rewards;
	
	private OUPBVI pbvi;
	
	public SampledPOMDP(OUPBVI pbvi) {
		states = new HashMap<State, State>();
		actions = new HashMap<Evidence, Evidence>();
		observations = new HashMap<Evidence, Evidence>();
		stateActions = new HashMap<State, Set<Evidence>>();
		obsWeights = new HashMap<Pair<State, Evidence>, Map<Evidence, Integer>>();
		nextBeliefs = new HashMap<Triplet<State, Evidence, Evidence>, Belief>();
		rewards = new HashMap<Pair<State, Evidence>, Double>();
		this.pbvi = pbvi;
	}
	
	public void addState(State s) {
		if (!states.containsKey(s)) {
			this.states.put(s, s);
			//System.out.println(s.getWorld());
		}
	}
	
	public void addStates(Collection<State> states) {
		for (State s : states)
			addState(s);
	}
	
	public void addStateActions(State s, Set<Evidence> actions) {
		s = this.states.get(s);
		Set<Evidence> actionsToAdd = new HashSet<Evidence>();
		for (Evidence a : actions) {
			Evidence existingAction = this.actions.get(a);
			if (existingAction == null) {
				this.actions.put(a, a);
				actionsToAdd.add(a);
			} else {
				actionsToAdd.add(existingAction);
			}
		}
		stateActions.put(s, actionsToAdd);
	}
	
	public void addObsWeights(State s, Evidence a, Evidence o, Integer count) {
		s = getState(s);
		a = actions.get(a);
		Evidence existingObs = observations.get(o);
		if (existingObs == null) {
			observations.put(o, o);
		} else {
			o = existingObs;
		}
		Pair<State, Evidence> sa = new Pair<State, Evidence>(s, a);
		if (!obsWeights.containsKey(sa)) {
			obsWeights.put(sa, new HashMap<Evidence, Integer>());
		}
		obsWeights.get(sa).put(o, count);
		System.out.println("sa->o cnt " + obsWeights.size());
		System.out.println("num states " + states.size());
	}
	
	public void addNextBelief(State s, Evidence a, Evidence o, Belief belief) {
		s = getState(s);
		a = actions.get(a);
		o = observations.get(o);
		Belief b = new Belief(pbvi);
		for (State state : belief.getStates()) {
			b.addState(getState(state), belief.getCount(state));
		}
		nextBeliefs.put(new Triplet<State, Evidence, Evidence>(s, a, o), b);
	}
	
	public void addReward(State s, Evidence a, Double reward) {
		s = getState(s);
		a = actions.get(a);
		rewards.put(new Pair<State, Evidence>(s, a), reward);
	}
	
	public Double getReward(State s, Evidence a) {
		return rewards.get(new Pair<State, Evidence>(s, a));
	}
	
	public Double getAvgReward(Belief b, Evidence a) {
		double total = 0;
		int count = 0;
		for (State s : b.getStates()) {
			int weight = b.getCount(s);
			//System.out.println("getReward: " + a + " " + getReward(s, a));
			total += getReward(s, a) * weight;
			count += weight;
		}
		return total/count;
	}
	
	public State getState(State s) {
		return states.get(s);
	}
	
	public Set<State> getPropagatedStates() {
		return stateActions.keySet();
	}
	
	public Set<Evidence> getActions(State s) {
		return stateActions.get(s);
	}
	
	public Set<Evidence> getActions(Belief b) {
		return getActions((State) Util.getFirst(b.getStates()));
	}
	
	public Belief getNextBelief(State s, Evidence a, Evidence o) {
		return nextBeliefs.get(new Triplet<State, Evidence, Evidence>(s, a, o));
	}
	
	public Belief getNextBelief(Belief b, Evidence a, Evidence o) {
		Belief next = new Belief(pbvi);
		for (State s : b.getStates()) {
			Belief n = getNextBelief(s, a, o);
			if (n == null) continue;
			next.addBelief(n);
		}
		return next;
	}
	
	public Map<Evidence, Integer> getObservations(State s, Evidence a) {
		return obsWeights.get(new Pair<State, Evidence>(s, a));
	}
	
	public Map<Evidence, Integer> getObservations(Belief b, Evidence a) {
		Map<Evidence, Integer> obs = new HashMap<Evidence, Integer>();
		for (State s : b.getStates()) {
			Map<Evidence, Integer> obsForState = getObservations(s, a);
			if (obsForState == null) continue;
			for (Evidence o : obsForState.keySet()) {
				if (!obs.containsKey(o)) {
					obs.put(o, 0);
				}
				obs.put(o, obs.get(o) + b.getCount(s));
			}
		}
		return obs;
	}

	public Set<State> getStates() {
		return states.keySet();
	}
}

class Pair<T1, T2> {
	T1 x;
	T2 y;
	
	public Pair(T1 a, T2 b) {
		x = a;
		y = b;
	}
	
	@Override
	public int hashCode() {
		return x.hashCode() ^ y.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair p = (Pair) o;
		return p.x.equals(x) && p.y.equals(y);
	}
}

class Triplet<T1, T2, T3> {
	T1 x;
	T2 y;
	T3 z;
	
	public Triplet(T1 a, T2 b, T3 c) {
		x = a;
		y = b;
		z = c;
	}
	
	@Override
	public int hashCode() {
		return x.hashCode() ^ y.hashCode() ^ z.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Triplet)) return false;
		Triplet t = (Triplet) o;
		return t.x.equals(x) && t.y.equals(y) && t.z.equals(z);
	}
}