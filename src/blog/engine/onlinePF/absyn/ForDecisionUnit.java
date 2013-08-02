package blog.engine.onlinePF.absyn;


public class ForDecisionUnit extends DecisionUnit{
	LoopAssignment la;
	DecisionUnit body;

	public ForDecisionUnit(LoopAssignment la, DecisionUnit body){
		this.la = la;
		this.body = body;
	}
	
	@Override
	public String getDecision(QueryResult q){
		String rtn="";
		for (String assignment : la.assignments){
			DecisionUnit.stack.put(la.loopVariable, assignment);
			rtn += body.getDecision(q);
			DecisionUnit.stack.remove(la.loopVariable);
		}
		return rtn;
	}
	
	@Override
	public String getQueries(int t) {
		String rtn = "";
		for (String assignment : la.assignments){
			DecisionUnit.stack.put(la.loopVariable, assignment);
			rtn += body.getQueries(t);
			stack.remove(la.loopVariable);
		}
		return rtn;
	}
	


}
