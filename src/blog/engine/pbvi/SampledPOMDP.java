package blog.engine.pbvi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import blog.common.Util;
import blog.model.Evidence;

public class SampledPOMDP {
	private Map<State, State> states;
	private Map<State, Set<Evidence>> stateActions;
	private Map<Pair<State, Evidence>, Map<Evidence, Integer>> obsWeights;
	private Map<Triplet<State, Evidence, Evidence>, Belief> nextBeliefs;
	
	public SampledPOMDP() {
		states = new HashMap<State, State>();
		stateActions = new HashMap<State, Set<Evidence>>();
		obsWeights = new HashMap<Pair<State, Evidence>, Map<Evidence, Integer>>();
		nextBeliefs = new HashMap<Triplet<State, Evidence, Evidence>, Belief>();
	}
	
	public void addState(State s) {
		this.states.put(s, s);
	}
	
	public void addStates(Collection<State> states) {
		for (State s : states)
			addState(s);
	}
	
	public void addStateActions(State s, Set<Evidence> actions) {
		stateActions.put(s, actions);
	}
	
	public void addObsWeights(State s, Evidence a, Evidence o, Integer count) {
		s = getState(s);
		Pair<State, Evidence> sa = new Pair<State, Evidence>(s, a);
		if (!obsWeights.containsKey(sa)) {
			obsWeights.put(sa, new HashMap<Evidence, Integer>());
		}
		obsWeights.get(sa).put(o, count);
	}
	
	public void addNextBelief(State s, Evidence a, Evidence o, Belief belief) {
		s = getState(s);
		Belief b = new Belief();
		for (State state : belief.getStates().keySet()) {
			b.addState(getState(state));
		}
		nextBeliefs.put(new Triplet<State, Evidence, Evidence>(s, a, o), b);
	}
	
	public State getState(State s) {
		return states.get(s);
	}
	
	public Set<Evidence> getActions(State s) {
		return stateActions.get(s);
	}
	
	public Set<Evidence> getActions(Belief b) {
		return getActions((State) Util.getFirst(b.getStates().keySet()));
	}
	
	public Belief getNextBelief(State s, Evidence a, Evidence o) {
		return nextBeliefs.get(new Triplet<State, Evidence, Evidence>(s, a, o));
	}
	
	public Belief getNextBelief(Belief b, Evidence a, Evidence o) {
		Belief next = new Belief();
		for (State s : b.getStates().keySet()) {
			Belief n = getNextBelief(s, a, o);
			if (n == null) continue;
			next.addStates(n.getStates());
		}
		return next;
	}
	
	public Map<Evidence, Integer> getObservations(State s, Evidence a) {
		return obsWeights.get(new Pair<State, Evidence>(s, a));
	}
	
	public Map<Evidence, Integer> getObservations(Belief b, Evidence a) {
		Map<Evidence, Integer> obs = new HashMap<Evidence, Integer>();
		for (State s : b.getStates().keySet()) {
			Map<Evidence, Integer> obsForState = getObservations(s, a);
			if (obsForState == null) continue;
			for (Evidence o : obsForState.keySet()) {
				if (!obs.containsKey(o)) {
					obs.put(o, 0);
				}
				obs.put(o, obs.get(o) + b.getStates().get(s));
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