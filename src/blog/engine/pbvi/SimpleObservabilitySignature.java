package blog.engine.pbvi;

import java.util.HashMap;
import java.util.HashSet;

import blog.bn.BayesNetVar;
import blog.engine.onlinePF.ObservabilitySignature;

public class SimpleObservabilitySignature extends ObservabilitySignature {
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SimpleObservabilitySignature)) return false;
		ObservabilitySignature os = (ObservabilitySignature) other;
		return this.observables.equals(os.observables) &&
				this.observedValues.equals(os.observedValues) &&
				this.unobservables.equals(os.unobservables);
	}
	
	@Override
	public int hashCode() {
		return observables.hashCode() ^ observedValues.hashCode() ^ unobservables.hashCode();
	}
	
	public static SimpleObservabilitySignature simplifyOS(ObservabilitySignature os) {
		SimpleObservabilitySignature newOS = new SimpleObservabilitySignature();
		newOS.observables = new HashSet<BayesNetVar>(os.observables);
		newOS.unobservables = new HashSet<BayesNetVar>(os.unobservables);
		newOS.observedValues = new HashMap<BayesNetVar, Object>(os.observedValues);
		newOS.myEvidence = os.getEvidence();
		return newOS;
	}
	
}
