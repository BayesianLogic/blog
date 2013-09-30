package blog.engine.onlinePF.absyn;


public class UnconditionalDecisionUnit extends DecisionUnit{
	String decisionTemplate;
	public UnconditionalDecisionUnit(String action){
		decisionTemplate = action;
	}
	@Override 
	public String getDecision(QueryResult q) {
		String rtn = "decide " + templateToString(decisionTemplate, q.timestep) + "=true;";
		if (isDoable(q))
			return rtn;
		else
			return "";
	}
	@Override
	public String getQueries(int t) {
		return "query observable<"+templateToString(decisionTemplate, t)+">;";
	}
	
	public boolean isDoable(QueryResult q){
		int t = q.timestep;
		Double queryResult = q.answerQuery("observable<"+templateToString(decisionTemplate, t)+">");
		Boolean rtn = false;
		rtn = queryResult.doubleValue() >= 1;
		return rtn;
	}
	
	@Override
	public void updateGenObjects(String s) {		
	}
}
