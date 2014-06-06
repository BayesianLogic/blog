package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import blog.model.SymbolEvidenceStatement;
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
	private LiftedProperties prevLiftedProperties;
	private LiftedProperties liftedProperties;
/*	
	public LiftedEvidence(Evidence evidence) {
		this(evidence, null);
	}*/
	
	/**
	 * There should be more information passed in to help convert the grounded decisionEvidence into a lifted one.
	 * For example, take the decision evidence statement "apply_assertGrandparent(a, c) = true" and assume a is fixed.
	 * The properties of c must be specified.
	 * 
	 * The belief after the evidence was obtained is necessary because LiftedProperties require
	 * NonGuaranteedObjects in the BayesNetVars. Evidence contains symbols that need to be translated to
	 * NonGuaranteedObjects. LiftedProperties can be written to handle symbols and terms instead of objects
	 * and BayesNetVars. These objects can really be instead replaced with dummy objects.
	 * A second reason to be resolved is that the history of each NonGuaranteedObject must come from somewhere.
	 * 
	 * @param evidence
	 */
	public LiftedEvidence(Evidence evidence, Belief b) {
		liftedProperties = new LiftedProperties();
		State s = (State) Util.getFirst(b.getStates());
		AbstractPartialWorld w = s.getWorld();

		int timestep = 0;
		Set<? extends BayesNetVar> evidenceVars = evidence.getEvidenceVars();
		Evidence newEvidence = new Evidence();
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
		evidence = evidence.replace(toReplace, emptyTimestep);
		
		Collection<ValueEvidenceStatement> ves = evidence.getValueEvidence();
		Collection<DecisionEvidenceStatement> des = evidence.getDecisionEvidence(); //TODO
		
		Set<DerivedVar> liftedVars = new HashSet<DerivedVar>();
		
		boolean hasLifted = false;
		/*for (ValueEvidenceStatement stmt : ves) {
			BayesNetVar var = stmt.getObservedVar();
			Object value = stmt.getObservedValue();
			if (var instanceof DerivedVar) {
				ArgSpec argSpec = ((DerivedVar) var).getArgSpec();
				if (!(argSpec instanceof FuncAppTerm)) {
					continue;
				}
				FuncAppTerm term = (FuncAppTerm) argSpec;
				if (!(term.getFunction() instanceof blog.model.RandomFunction)) continue;
				RandomFunction function = (RandomFunction) term.getFunction();
				
				// if an observable_ function, skip
				//if (function.getObservableFun() == null) continue;

				ArgSpec[] args = term.getArgs();
				List<Object> newArgs = new ArrayList<Object>();
				boolean hasNgo = false;
				for (ArgSpec arg : args) {
					Object newArg = arg.evaluate(w);
					newArgs.add(arg); //using symbol object instead
					//newArgs.add(newArg);
					if (newArg instanceof NonGuaranteedObject) {
						hasNgo = true;
						liftedProperties.addObject(arg); //using symbol object instead
						//liftedProperties.addObject(newArg);
					}
				}
				if (hasNgo) {
					// NOTE: the following RandFuncAppVar is not "valid" in the sense that
					// its arguments contain symbols rather than their corresponding objects
					RandFuncAppVar newVar = new RandFuncAppVar(function, newArgs);
					liftedProperties.addProperty(newVar, value);
					hasLifted = true;
					liftedVars.add((DerivedVar) var);
				}

			}
		}*/
		
		//if (hasLifted)
			//System.out.println("Lifted" + liftedProperties);
		

		for (ValueEvidenceStatement stmt : ves) {
			if (liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addValueEvidence(stmt);
		}

		for (DecisionEvidenceStatement stmt : des) {
			if (liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addDecisionEvidence(stmt);
		}
		for (SymbolEvidenceStatement stmt : evidence.getSymbolEvidence()) {
			if (liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addSymbolEvidence(stmt);
		}

		newEvidence.compile();
		this.evidence = newEvidence;
		//System.out.println("evidence" + this.evidence);
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
		return this.evidence.equals(otherEvidence.evidence) && this.liftedProperties.equals(otherEvidence.liftedProperties);
	}
	
	@Override
	public int hashCode() {
		return this.evidence.hashCode();
	}
	
	@Override
	public String toString() {
		return this.evidence.toString() + " Lifted: " + this.liftedProperties.toString();
	}

	public Evidence getStoredEvidence() {
		return evidence;
	}

	public LiftedProperties getLifted() {
		return liftedProperties;
	}
}
