package blog.engine.pbvi;

import java.util.HashMap;
import java.util.Map;

import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;

public class AlphaVector {
	private Map<State, Double> values;
	
	public AlphaVector() {
		values = new HashMap<State, Double>();
	}
	
	public Double getValue(State s) {
		return values.get(s);
	}
	
	public Double getValue(Belief b) {
		Double value = 0D;
		int total = 0;
		for (State s : b.getStates()) {
			Double v = getValue(s);
			if (v == null) return null;
			value += v * b.getCount(s);
			total += b.getCount(s);
		}
		return value/total;
	}
	
	public void setValue(State s, Double value) {
		values.put(s, value);
	}
	
	public void setValue(PartialWorld w, Double value) {
		values.put(null, value);
	}
	
	public void normalizeValues(Integer denom) {
		for (State s : values.keySet()) {
			setValue(s, getValue(s)/denom);
		}
	}
}
