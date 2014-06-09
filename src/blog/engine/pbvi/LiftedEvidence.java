package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.Arrays;
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
import blog.model.DecisionFunction;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.NonGuaranteedObject;
import blog.model.RandomFunction;
import blog.model.SkolemConstant;
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

	public LiftedEvidence(Evidence evidence, Belief b) {
		this(evidence, null, null);
	}
	
	/**
	 * @param evidence
	 */
	public LiftedEvidence(Evidence evidence, Belief b, LiftedProperties liftedHistory) {
		liftedProperties = new LiftedProperties();

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
		if (liftedHistory == null)
			evidence = evidence.replace(toReplace, emptyTimestep);
		
		Collection<ValueEvidenceStatement> ves = evidence.getValueEvidence();
		Collection<DecisionEvidenceStatement> des = evidence.getDecisionEvidence(); //TODO
		Collection<Object> statements = new ArrayList<Object>(ves);
		statements.addAll(des);
		
		Set<DerivedVar> liftedVars = new HashSet<DerivedVar>();
		
		boolean hasLifted = false;
		for (Object stmt : statements) {
			BayesNetVar var = null; 
			Object value = null; 
			if (stmt instanceof ValueEvidenceStatement) {
				var = ((ValueEvidenceStatement) stmt).getObservedVar();
				value = ((ValueEvidenceStatement) stmt).getObservedValue();
			} else if (stmt instanceof DecisionEvidenceStatement) {
				var = ((DecisionEvidenceStatement) stmt).getObservedVar();
				value = ((DecisionEvidenceStatement) stmt).getObservedValue();
			} else {
				System.err.println("LiftedEvidence: " + stmt + " not a ValueEvidenceStatement or DecisionEvidenceStatement");
				System.exit(1);
			}
			
			if (var instanceof DerivedVar) {
				ArgSpec argSpec = ((DerivedVar) var).getArgSpec();
				if (!(argSpec instanceof FuncAppTerm)) {
					continue;
				}
				FuncAppTerm term = (FuncAppTerm) argSpec;
				RandomFunction function = null;
				if (term.getFunction() instanceof blog.model.RandomFunction)
					function = (RandomFunction) term.getFunction();
				else if (term.getFunction() instanceof blog.model.DecisionFunction) {
					DecisionFunction f = (DecisionFunction) term.getFunction();
					function = new RandomFunction(f.getName(), Arrays.asList(f.getArgTypes()), f.getRetType(), null);
					//System.out.println("Decision " + function);
				} else
					continue;
				// if an observable_ function, skip
				//if (function.getObservableFun() == null) continue;

				ArgSpec[] args = term.getArgs();
				List<Object> newArgs = new ArrayList<Object>();
				boolean hasNgo = false;
				for (ArgSpec arg : args) {
					newArgs.add(arg); //using symbol object instead
					if (!(arg instanceof FuncAppTerm)) continue;
					FuncAppTerm fat = (FuncAppTerm) arg;
					if (!(fat.getFunction() instanceof SkolemConstant)) continue;
					//Object newArg = arg.evaluate(w);
					//newArgs.add(newArg);
					//System.out.println(newArg);
					//if (newArg instanceof NonGuaranteedObject) {
					hasNgo = true;
					liftedProperties.addObject(arg); //using symbol object instead
					//liftedProperties.addObject(newArg);
					//}
				}
				if (hasNgo) {
					// NOTE: the following RandFuncAppVar is not "valid" in the sense that
					// its arguments contain symbols rather than their corresponding objects.
					// TODO: change  liftedProperties to use Terms instead of Bayes net vars
					RandFuncAppVar newVar = new RandFuncAppVar(function, newArgs);
					
					liftedProperties.addProperty(newVar, value);
					hasLifted = true;
					liftedVars.add((DerivedVar) var);
				}
			}
		}
		
		//if (hasLifted)
			//System.out.println("Lifted" + liftedProperties);
		

		for (ValueEvidenceStatement stmt : ves) {
			if (liftedHistory != null && liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addValueEvidence(stmt);
		}

		for (DecisionEvidenceStatement stmt : des) {
			if (liftedHistory != null && liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addDecisionEvidence(stmt);
		}
		for (SymbolEvidenceStatement stmt : evidence.getSymbolEvidence()) {
			if (liftedVars.contains(stmt.getObservedVar())) continue;
			newEvidence.addSymbolEvidence(stmt);
		}
		
		if (liftedHistory != null)
			liftedProperties.add(liftedHistory);

		newEvidence.compile();
		this.evidence = newEvidence;
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
		//liftedProperties.replace(emptyTimestep, replace);
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

	public LiftedProperties getLiftedProperties() {
		return liftedProperties;
	}
}
