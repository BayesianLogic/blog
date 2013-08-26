package blog.engine.onlinePF.absyn;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blog.common.Util;
import blog.engine.onlinePF.absyn.ConditionChecker.Op;
import blog.model.ArgSpec;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;


public class UnconditionalDecisionUnit extends DecisionUnit{
	String decisionTemplate;
	
	ArrayList<ConditionChecker> ccs = new ArrayList<ConditionChecker>();
	
	
	public UnconditionalDecisionUnit(String action){
		decisionTemplate = action;
		
		
		
		Evidence ev = new Evidence();
		List<Query> qu = Util.list();
		parseAndTranslateEvidence(ev, qu, new StringReader("decide " + decisionTemplate + "=true;"));		
		ev.checkTypesAndScope(Model.curMod);
		if (ev.compile()!=0){
			System.err.println("uc compile failed");
			System.exit(1);
		}
		ArgSpec decargspec = ((DecisionEvidenceStatement)Util.getFirst(ev.getDecisionEvidence())).getLeftSide();
		FuncAppTerm fat = (FuncAppTerm) decargspec;
		ArrayList<String> arl = new ArrayList();
		for (ArgSpec v : fat.getArgs()){
			arl.addAll(convert(v));
		}
		for (String s : arl){
			ConditionChecker n = new ConditionChecker(s, (double) 1, Op.EQ);
			ccs.add(n);
		}
		
	}
	
	public static ArrayList<String> convert(ArgSpec a){
		ArrayList<String> rtn = new ArrayList<String>();
		Model m = Model.curMod;
		if (a instanceof FuncAppTerm){
			if (((FuncAppTerm)a).getFunction() instanceof RandomFunction){
				RandomFunction rf = (RandomFunction)((FuncAppTerm)a).getFunction();
				if (rf.getObservableFun()==null){
					System.err.println("Error: non observable argument to decision");
					System.exit(1);
				}
				else {
					String argstr = Arrays.toString(((FuncAppTerm)a).getArgs());
					argstr = argstr.substring(1,argstr.length()-1);
					rtn.add("" + rf.getObservableFun().getName() + "(" + argstr + ")");
				}
			}
			ArgSpec[] args = (((FuncAppTerm)a)).getArgs();
			for (ArgSpec arg : args){
				rtn.addAll(convert(arg));
			}
		}
		return rtn;
	}
	
	@Override
	public String getDecision(QueryResult q) {
		String rtn = "decide " + templateToString(decisionTemplate, q.timestep) + "=true;";
		return rtn;
	}
	@Override
	public String getQueries(int t) {
		String rtn = "";
		for (ConditionChecker check : ccs)
			rtn += "query " + templateToString(check.queryTemplate, t) + ";";
		return rtn;
	}
	
	//need to fix the error message for empty evidence string inputs
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(Model.curMod, e, q, new ErrorMsg.quietErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg(parse.getParseResult());
		return true;
	}
}
