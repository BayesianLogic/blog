package blog.engine.pbvi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.engine.onlinePF.absyn.PolicyModel;
import blog.model.ArgSpec;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.FuncAppTerm;

public class FiniteStatePolicy extends PolicyModel {
	private AlphaVector alpha;
	private Evidence action;
	private Map<Evidence, FiniteStatePolicy> successors;
	
	private Set<String> requiredSymbols;
	
	public FiniteStatePolicy(Evidence action, Map<Evidence, FiniteStatePolicy> successors) {
		this.action = action;
		this.successors = successors;
		
		/*requiredSymbols = new HashSet<String>();
		DecisionEvidenceStatement a = (DecisionEvidenceStatement) action.getDecisionEvidence().iterator().next();
		FuncAppTerm f = ((FuncAppTerm) a.getLeftSide());
		ArgSpec[] args = f.getArgs();
		args[0].*/
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
	
	//works only for a tree right now
	public String toDotString(String name) {
		String result = name + " [label=\"" + action + "\"];\n";
		int i = 0;
		for (Evidence o : successors.keySet()) {
			String nextName = name + "_" + i;
			String evidenceString = "";
			Collection valueEvidence = o.getValueEvidence();
			for (Object v : valueEvidence) {
				evidenceString += v.toString() + "\\n";
			}
			Collection symbolEvidence = o.getSymbolEvidence();
			for (Object s : symbolEvidence) {
				evidenceString += s.toString() + "\\n";
			}
			result = result + name + " -> " + nextName + " [label=\"" + evidenceString + "\"];\n";
			result += successors.get(o).toDotString(nextName);
			i++;
		}
		
		return result;
	}
	
	public Boolean isApplicable(Belief b) {
		return true;
	}
}
