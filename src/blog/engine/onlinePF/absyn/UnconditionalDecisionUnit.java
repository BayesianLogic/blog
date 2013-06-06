package blog.engine.onlinePF.absyn;


public class UnconditionalDecisionUnit extends DecisionUnit{
	String decisionTemplate;
	public UnconditionalDecisionUnit(String action){
		decisionTemplate = action;
	}
	@Override
	public String getDecision(QueryResult q) {
		String rtn = "decide " + templateToString(decisionTemplate, q.timestep) + "=true;";
		return rtn;
	}
	@Override
	public String getQueries(int t) {
		return "";
	}
	

}
