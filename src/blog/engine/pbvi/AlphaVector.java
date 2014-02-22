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
		Double value = values.get(s);
		if (value == null) return -1000D;
		return value;
	}
	
	public Double getValue(PartialWorld w) {
		return null;
	}
	
	//TODO: remove
	public Double getValue(Belief b) {
		Double value = 0D;
		for (State s : b.getStates().keySet()) {
			value += getValue(s);
		}
		return value/b.getStates().size();
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
