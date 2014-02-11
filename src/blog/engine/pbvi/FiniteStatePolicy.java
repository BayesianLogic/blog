package blog.engine.pbvi;

import java.util.Map;

import blog.engine.onlinePF.PolicyModel;
import blog.model.Evidence;

public class FiniteStatePolicy extends PolicyModel {
	private AlphaVector alpha;
	private Evidence action;
	private Map<Evidence, FiniteStatePolicy> successors;
	
	public FiniteStatePolicy(Evidence action, Map<Evidence, FiniteStatePolicy> successors) {
		this.action = action;
		this.successors = successors;
	}
	
	public AlphaVector getAlphaVector() { 
		return alpha; 
	}
	
	public void setAlphaVector(AlphaVector alpha) { 
		this.alpha = alpha; 
	}
}
