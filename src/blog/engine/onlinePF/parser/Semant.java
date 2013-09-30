package blog.engine.onlinePF.parser;

import blog.engine.onlinePF.absyn.Absyn;
import blog.engine.onlinePF.absyn.ActionStmt;
import blog.engine.onlinePF.absyn.ConditionChecker;
import blog.engine.onlinePF.absyn.DecisionUnit;
import blog.engine.onlinePF.absyn.ForDecisionUnit;
import blog.engine.onlinePF.absyn.IfDecisionUnit;
import blog.engine.onlinePF.absyn.IfStmt;
import blog.engine.onlinePF.absyn.ForStmt;
import blog.engine.onlinePF.absyn.OpExpr;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.absyn.Stmt;
import blog.engine.onlinePF.absyn.StmtList;
import blog.engine.onlinePF.absyn.UnconditionalDecisionUnit;

public class Semant {
	public PolicyModel pm;
	public Semant(){
		this.pm = new PolicyModel();
	}
	public void transPolicy(Absyn absyn){
		if (absyn instanceof StmtList){
			transStmtList((StmtList) absyn);
		}
	}
	private void transStmtList (StmtList stl){
		while (stl != null){
			transStmt ((Stmt) stl.head);
			stl = stl.next;
		}
	}
	private void transStmt (Stmt st){
		if (st == null){
			System.err.println("null statement in transStmt in Semant.java");
			System.exit(1);
		}
		if (st instanceof IfStmt){
			pm.decisionUnits.add(transIfStmt((IfStmt)st));
		}
		else if (st instanceof ForStmt){
			pm.decisionUnits.add(transForStmt((ForStmt)st));
		}
		else{
			Boolean a = st instanceof IfStmt;
			System.err.println("non-IfStmt supplied to transStmt in Semant.java");
			System.exit(1);
		}
	}
	private DecisionUnit transIfStmt(IfStmt ifs){
		ConditionChecker c = transTest((OpExpr) ifs.test); 
		DecisionUnit ift = transIfOrActionStmt(ifs.thenclause);
		DecisionUnit iff = transIfOrActionStmt(ifs.elseclause);
		IfDecisionUnit ifd = new IfDecisionUnit(c, ift, iff);
		return ifd;
	}

	private DecisionUnit transForStmt(ForStmt fos){
		DecisionUnit act = transIfOrActionStmt(fos.body);
		ForDecisionUnit fod = new ForDecisionUnit(fos.type, fos.var, act);
		return fod;
	}

	private DecisionUnit transIfOrActionStmt(Stmt s){
		if (s instanceof ActionStmt)
			return transActionStmt((ActionStmt) s);
		else if (s instanceof IfStmt)
			return transIfStmt((IfStmt) s);
		else if (s instanceof ForStmt)
			return transForStmt((ForStmt) s);
		else {
			System.err.println("DecisionUnit: unable to find a match in transIfOrActionStmt");
			System.exit(1);
			return null;
		}
	}

	private UnconditionalDecisionUnit transActionStmt(ActionStmt s){
		return new UnconditionalDecisionUnit (s.action);
	}

	private ConditionChecker transTest(OpExpr exp){
		ConditionChecker.Op op = null;
		switch (exp.op){
		case GEQ: op = ConditionChecker.Op.GEQ; break;
		case LEQ: op = ConditionChecker.Op.LEQ; break;
		case GT: op = ConditionChecker.Op.GT; break;
		case LT: op = ConditionChecker.Op.LT; break;
		case EQ: op = ConditionChecker.Op.EQ; break;
		case NEQ: op = ConditionChecker.Op.NEQ; break;
		}

		ConditionChecker c = new ConditionChecker(exp.query, (Double) exp.threshold, op);

		return c;

	}
}
