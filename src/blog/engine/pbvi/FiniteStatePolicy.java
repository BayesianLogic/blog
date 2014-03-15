package blog.engine.pbvi;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.model.ArgSpec;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Term;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;

public class FiniteStatePolicy extends PolicyModel {
	private AlphaVector alpha;
	private Evidence action;
	private Map<Evidence, FiniteStatePolicy> successors;
	private Map<Evidence, String> notes;
	private int id;
	private Set<ArgSpec> requiredTerms;
	
	public FiniteStatePolicy(Evidence action, Map<Evidence, FiniteStatePolicy> successors) {
		this.id = count;
		count++;
		this.action = action;
		this.successors = successors;
		this.notes = new HashMap<Evidence, String>();
		
		requiredTerms = new HashSet<ArgSpec>();
		DecisionEvidenceStatement a = (DecisionEvidenceStatement) action.getDecisionEvidence().iterator().next();
		FuncAppTerm f = ((FuncAppTerm) a.getLeftSide());
		ArgSpec[] args = f.getArgs();
		requiredTerms.addAll(Arrays.asList(args));
		for (Evidence e : successors.keySet()) {
			Set<ArgSpec> futureRequiredTerms = new HashSet<ArgSpec>(successors.get(e).requiredTerms);
			Set<? extends BayesNetVar> vars = e.getEvidenceVars();
			for (BayesNetVar v : vars) {
				if (v instanceof BasicVar) {
					Term t = ((BasicVar) v).getCanonicalTerm();
					futureRequiredTerms.remove(t);
				}
				if (v instanceof DerivedVar) {
					ArgSpec s = ((DerivedVar) v).getArgSpec();
					futureRequiredTerms.remove(s);
				}
			}
			requiredTerms.addAll(futureRequiredTerms);
		}
	}
	
	public AlphaVector getAlphaVector() { 
		return alpha;
	}
	
	public void setAlphaVector(AlphaVector alpha) { 
		this.alpha = alpha; 
	}
	
	public String toString() {
		String s = "action: " + action + "\n";
		s += successors;
		return s;
	}
	
	public Evidence getAction() {
		return action;
	}
	
	//works only for a tree right now
	public String toDotString(String name) {
		Map<FiniteStatePolicy, String> included = new HashMap<FiniteStatePolicy, String>();
		return toDotStringHelper(name + "_" + id, included);
	}
	
	private String toDotStringHelper(String name, Map<FiniteStatePolicy, String> included) {
		included.put(this, name);
		String result = name + " [label=\"" + action + "\"];\n";
		int i = 0;
		for (Evidence o : successors.keySet()) {
			String evidenceString = "";
			Collection valueEvidence = o.getValueEvidence();
			for (Object v : valueEvidence) {
				evidenceString += v.toString() + "\\n";
			}
			Collection symbolEvidence = o.getSymbolEvidence();
			for (Object s : symbolEvidence) {
				evidenceString += s.toString() + "\\n";
			}
			
			FiniteStatePolicy contingentPolicy = successors.get(o);
			String nextName = name + "_" + i;
			if (included.containsKey(contingentPolicy)) {
				nextName = included.get(contingentPolicy);
			} else {
				result += contingentPolicy.toDotStringHelper(nextName, included);
			}
			result = result + name + " -> " + nextName + " [label=\"" + evidenceString +  " " + getNote(o) + "\"];\n";
			
			i++;
		}
		
		return result;
	}
	
	public Boolean isApplicable(AbstractPartialWorld w) {
		Map<BayesNetVar, BayesNetVar> observables = w.getObservableMap();
		for (ArgSpec s : requiredTerms) {
			Term t = (Term) s;
			BayesNetVar var = s.getVariable();
			if (!observables.containsKey(var)) {
				Object o = w.getValue(var);
				if (o == null) return false;
				if (t.getType().isBuiltIn()) continue;
				if (!t.getType().getGuaranteedObjects().contains(o))
					return false;
			}
		}
		return true;
	}
	
	public Boolean isApplicable(Belief b) {
		for (State s : b.getStates())
			return isApplicable(s.getWorld());
		return false;
	}

	public FiniteStatePolicy getNextPolicy(Evidence latestEvidence) {
		return successors.get(latestEvidence);
	}
	
	public FiniteStatePolicy getApplicableNextPolicy(Evidence latestEvidence, Belief nextBelief) {
		for (Evidence o : successors.keySet()) {
			if (successors.get(o).isApplicable(nextBelief)) {
				return successors.get(o);
			}
		}
		return null;
	}

	public void setNextPolicy(Evidence obs,
			FiniteStatePolicy nextPolicy) {
		if (nextPolicy == null) {
			System.err.println("You can't set next policy to null");
			new Exception().printStackTrace();
			System.exit(0);
		}
		successors.put(obs, nextPolicy);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FiniteStatePolicy)) {
			return false;
		}
		FiniteStatePolicy p = (FiniteStatePolicy) other;
		if (!p.getAction().equals(this.getAction()))
			return false;
		return this.successors.equals(p.successors);
	}
	
	//after hashcode is queried, prevent further changes
	private Integer setHashCode = null;
	
	@Override
	public int hashCode() {
		if (setHashCode == null)
			setHashCode =  action.hashCode() ^ successors.hashCode();
		return setHashCode;
	}
	
	public boolean merge(FiniteStatePolicy policy) {
		 if (!action.equals(policy.action)) return false;
		 for (Evidence o : successors.keySet()) {
			 if (policy.successors.containsKey(o) &&
					 !successors.get(o).equals(policy.successors.get(o))) {
				 return false;
			 }
		 }
		 
		 for (Evidence o : policy.successors.keySet()) {
			 successors.put(o, policy.getNextPolicy(o));
			 addObsNote(o, "merged");
		 }
		 this.alpha = null;
		 return true;
	}
	
	public void addObsNote(Evidence obs, String note) {
		notes.put(obs, note);
	}
	
	public String getNote(Evidence obs) {
		String note = notes.get(obs);
		if (note == null) {
			return "";
		}
		return note;
	}

	public int getID() {
		return id;
	}
	
	private static int count = 0;
	
}
