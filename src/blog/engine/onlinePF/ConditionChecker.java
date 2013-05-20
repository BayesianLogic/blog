package blog.engine.onlinePF;
public class ConditionChecker{
	String queryTemplate;
	Double threshold;
	Op op;
	public ConditionChecker(String condition, Double threshold, Op com){
		this.queryTemplate = condition;
		this.threshold = threshold;
		this.op = com;
	}
	
	public boolean isTrue(QueryResult q){
		int t = q.timestep;
		Double queryResult = q.answerQuery(DecisionUnit.templateToString(queryTemplate, t));
		Boolean rtn = false;
		switch (op){
		case GEQ : rtn = queryResult.doubleValue() >= threshold.doubleValue(); break;
		case LEQ : rtn = queryResult.doubleValue() <= threshold.doubleValue(); break;
		case GT : rtn = queryResult.doubleValue() > threshold.doubleValue(); break;
		case LT : rtn = queryResult.doubleValue() < threshold.doubleValue(); break;
		case EQ : rtn = queryResult.doubleValue() == threshold.doubleValue(); break;
		case NEQ : rtn = queryResult.doubleValue() != threshold.doubleValue(); break;
		}
		return rtn;
	}

	public static enum Op {
		GEQ, LEQ, GT, LT, EQ, NEQ
	}
}