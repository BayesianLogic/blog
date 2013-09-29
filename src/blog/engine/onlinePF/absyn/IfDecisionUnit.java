package blog.engine.onlinePF.absyn;


public class IfDecisionUnit extends DecisionUnit{

	/*
	public static void main (String[] foo){
		IfPolicyUnit a = new IfPolicyUnit();
		//pclause uni = new pclause("pos(t)", "do this(t)", IfPolicyUnit.comparator.ge, 0.5);
		//a.clauses.add(uni);
		//String qr = "Distribution of values for pos(@0)\n		True	1\n	False 0 \n-----\n	Distribution of values for pos(@1)\n		True	2\n	-----\n";
		//if (uni.applies(qr, 0))
		//	System.out.println("works");
	}*/
	
	private ConditionChecker check;
	private DecisionUnit iftrue;
	private DecisionUnit iffalse;
	

	public IfDecisionUnit(ConditionChecker c, DecisionUnit ift, DecisionUnit iff){
		check = c;
		iftrue = ift;
		iffalse = iff;
	}
	
	@Override
	public String getDecision(QueryResult q){
		if (check.isTrue(q))
			return iftrue.getDecision(q);
		else
			return iffalse.getDecision(q);
	}
	
	@Override
	public String getQueries(int t) {
		String rtn = "";
		rtn += "query " + templateToString(check.queryTemplate, t) + ";";
		rtn += iftrue.getQueries(t);
		rtn += iffalse.getQueries(t);
		return rtn;	
	}

	@Override
	public void updateGenObjects(String s) {
		iftrue.updateGenObjects(s);
		iffalse.updateGenObjects(s);
		
	}
	


}
