package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import blog.DBLOGUtil;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.NonGuaranteedObject;
import blog.model.RandomFunction;
import blog.model.Term;
import blog.model.ValueEvidenceStatement;
import blog.world.AbstractPartialWorld;

/**
 * This is a wrapper for evidence 
 * to represent lifted actions and observations. 
 * For now, it'll just be time independent.
 */
public class LiftedEvidence {
	private static Term emptyTimestep;
	
	static {
		emptyTimestep = BuiltInTypes.REAL.getCanonicalTerm(Float.NaN);
	}
	
	private Evidence evidence;
	private LiftedObjects prevliftedObjects;
	private LiftedObjects liftedObjects;
/*	
	public LiftedEvidence(Evidence evidence) {
		this(evidence, null);
	}*/
	
	/**
	 * There should be more information passed in to help convert the grounded decisionEvidence into a lifted one.
	 * For example, take the decision evidence statement "apply_assertGrandparent(a, c) = true" and assume a is fixed.
	 * The properties of c must be specified.
	 * 
	 * @param evidence
	 */
	public LiftedEvidence(Evidence evidence, Belief b) {
		liftedObjects = new LiftedObjects();
		State s = (State) Util.getFirst(b.getStates());
		AbstractPartialWorld w = s.getWorld();
		Collection<ValueEvidenceStatement> ves = evidence.getValueEvidence();
		Collection<DecisionEvidenceStatement> des = evidence.getDecisionEvidence(); //TODO
		
		for (ValueEvidenceStatement stmt : ves) {
			BayesNetVar var = stmt.getObservedVar();
			if (var instanceof DerivedVar) {
				ArgSpec argSpec = ((DerivedVar) var).getArgSpec();
				if (argSpec instanceof FuncAppTerm) {
					FuncAppTerm t = (FuncAppTerm) argSpec;
					if (!(t.getFunction() instanceof blog.model.RandomFunction)) continue;
					ArgSpec[] args = t.getArgs();
					List<Object> newArgs = new ArrayList<Object>();
					boolean hasNgo = false;
					for (ArgSpec arg : args) {
						Object newArg = arg.evaluate(w);
						newArgs.add(newArg);
						if (newArg instanceof NonGuaranteedObject) {
							hasNgo = true;
							liftedObjects.addObject((NonGuaranteedObject) newArg);
						}
					}
					if (hasNgo) {
						RandFuncAppVar newVar = new RandFuncAppVar((RandomFunction) t.getFunction(), newArgs);
						liftedObjects.addProperty(newVar, w.getValue(newVar));
					}
				}
			}
		}

		//TODO: make sure getEvidenceVars is correct
		int timestep = 0;
		Set<? extends BayesNetVar> evidenceVars = evidence.getEvidenceVars();
		if (evidenceVars.size() > 0) {
			for (BayesNetVar var : evidenceVars) {
				timestep = DBLOGUtil.getTimestepIndex(var);
				if (timestep >= 0) break;
			}
			
			if (timestep < 0) {
				System.out.println("Evidence has no timestep? " + evidence);
				new Exception().printStackTrace();
				System.exit(0);
			}
		}
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
	
	public Evidence getEvidence(int timestep) {
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
