package blog.engine.pbvi;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.model.BuiltInTypes;
import blog.model.Evidence;
import blog.model.Term;

/**
 * This is a wrapper for evidence 
 * to represent lifted actions and observations. 
 * For now, it'll just be time independent.
 */
public class LiftedEvidence {
	private static Term emptyTimestep;
	
	static {
		emptyTimestep = BuiltInTypes.TIMESTEP.getCanonicalTerm(BuiltInTypes.TIMESTEP.getGuaranteedObject(0));
	}
	
	private Evidence evidence;
	
	/**
	 * There should be more information passed in to help convert the grounded decisionEvidence into a lifted one.
	 * For example, take the decision evidence statement "apply_assertGrandparent(a, c) = true" and assume a is fixed.
	 * The properties of c must be specified.
	 * 
	 * @param evidence
	 */
	public LiftedEvidence(Evidence evidence) {
		int timestep = DBLOGUtil.getTimestepIndex(Util.getFirst(evidence.getEvidenceVars()));
		Term toReplace = BuiltInTypes.TIMESTEP.getCanonicalTerm(BuiltInTypes.TIMESTEP.getGuaranteedObject(timestep));	
		this.evidence = evidence.replace(toReplace, emptyTimestep);
	}
	
	/**
	 * Returns the grounded version of the evidence.
	 * The belief should contain enough information to ground the evidence.
	 * @param b
	 * @return
	 */
	public Evidence getEvidence(Belief b) {
		int timestep = b.getTimestep();
		Term replace = BuiltInTypes.TIMESTEP.getCanonicalTerm(BuiltInTypes.TIMESTEP.getGuaranteedObject(timestep));
		Evidence grounded = evidence.replace(emptyTimestep, replace);
		return grounded;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LiftedEvidence))
			return false;
		LiftedEvidence otherEvidence = (LiftedEvidence) other;
		return this.evidence.equals(otherEvidence.evidence);
	}
	
	@Override
	public int hashCode() {
		return this.evidence.hashCode();
	}
	
	@Override
	public String toString() {
		return this.evidence.toString();
	}

	public Evidence getStoredEvidence() {
		return evidence;
	}
}
