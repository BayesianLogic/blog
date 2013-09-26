package figaro;

import figaro.type.*;

import java.util.*;

import blog.absyn.*;
import blog.msg.*;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Important!
 *  Class used to Build Figaro Structure
 *  and Semantic Check!
 *  
 *  @Warning Please make sure the input program can be correctly run in BLOG
 */

public class FigaroSemant {
	private ErrorMsg errorMsg;
	private Figaro figaro;
	
	//TODO: Do add Key Words in FIGARO here
	public final static String[] keyWords = {"var","val","Element","ElementCollection","class","extends"
				,"public","private","abstract","Object","main","def","return","If","if","then","else","Apply","Inject"
				,"Constant","Select","CPD","Universe","import","package","Double","Int","String","Boolean"
				,"Chain","protected","void","new","Any","null","Null","nil","Nil","Array","List","MakeList","Symbol"};
	
	public FigaroSemant(Figaro _figaro, ErrorMsg _errorMsg)
	{
		errorMsg = _errorMsg; figaro = _figaro; 
	}
	
	ArrayList<Expression> transExprList(ExprList e) {
		ArrayList<Expression> args = new ArrayList<Expression>();
		for (; e != null; e = e.next)
			args.add(transExpr(e.head));
		return args;
	}
	
	Expression transExpr(DistributionExpr e) {
		Distribution dis = new Distribution();
		//TODO: to support Dirichlet and MultiVarGaussian
		
		if(!dis.initSymbol(e.name.toString()))
		{
			error(e.line, e.col, "Distribution <"+e.name.toString()+"> is not supported");
			return null;
		}
		
		dis.setParam(transExprList(e.args));
		return dis;
	}
	
	Expression transExpr(BooleanExpr e) {
		return new Constant(Boolean.toString(e.value));
	}
	Expression transExpr(DoubleExpr e) {
		return new Constant(Double.toString(e.value));
	}
	Expression transExpr(IntExpr e) {
		return new Constant(Integer.toString(e.value));
	}
	Expression transExpr(StringExpr e) {
		if(e.value.startsWith("\"")) return new Constant(e.value);
		return new Constant("\""+e.value+"\"");
	}
	
	Expression transExpr(NumberExpr e) {
		Object r = transExpr(e.values);
		if (r instanceof SetTerm) {
			SetTerm s = (SetTerm) r;
			if(s.paramSize() > 0)
			{
				error(e.line, e.col, "We only support explicit set!");
				return null;
			}
			return new NumberReference(s.getType());
		} else {
			error(e.line, e.col, "Number expression expecting implicit set");
		}
		return null;
	}
	
	Expression transExpr(ExplicitSetExpr e) {
		SetTerm set = new SetTerm();
		
		ExprList currTerm = e.values;
		while (currTerm != null) {
			Expression term = transExpr(currTerm.head);
			if(term instanceof Variable)
			{
				Variable var = (Variable) term;
				if(set.getType().length() < 1 || set.getType().equals(var.getType())){
					set.addParam(var);
					if(set.getType().length() < 1 && var.getType().length() > 0)
						set.setType(var.getType());
					currTerm = currTerm.next;
				} else
				{
					error(currTerm.line,currTerm.col,"Type not macth!");
					return set;
				}
			} else
			{
				error(currTerm.line,currTerm.col,"We only support variable in < Explicit Set >");
				return set;
			}
			
			currTerm = currTerm.next;
		}
		
		if(set.paramSize() == 0)
		{
			error(e.line,e.col,"We Do Not Support Empty Set!");
		}
		
		return set;
	}
	
	Expression transExpr(ImplicitSetExpr e) {
		String typ = getNameType(e.typ);
		SetTerm set = new SetTerm(typ);
		
		if (e.cond != null) {
			//TODO: make condition useful
			// Note : now we do not support implicit set with condition
			
			error(e.cond.line,e.cond.col,"Currently we do not support < Implicit Set with Condition >");
			return set;
		}
		return set;
	}
	
	IfTerm transExpr(IfExpr e) {
		IfTerm term = new IfTerm();
		
		if(e.test == null ||
		   e.thenclause == null)
		{
			error(e.line, e.col, "We do not support < If Statement > with Missing Parameter on Condition and ThenClause!");
			return term;
		}
		
		term.addParam(transExpr(e.test));
		term.addParam(transExpr(e.thenclause));
		if(e.elseclause != null)term.addParam(transExpr(e.elseclause));
		else term.addParam(new Constant("null"));
		
		return term;
	}
	
	ArrayTerm transExpr(ListInitExpr e)
	{	
		ArrayTerm term = new ArrayTerm();
		ExprList currTerm = e.values;
		while(currTerm != null)
		{
			Expression t = transExpr(currTerm.head);
			
			if((t instanceof Constant) || (t instanceof ArrayTerm) || (t instanceof Variable)) {
				if(!term.addParam(t))
				{
					error(currTerm.line, currTerm.col, "Error Init for < Array >");
				}
			}
			else
			{
				error(currTerm.line, currTerm.col, "We only support < Array Type > for Constant Value or Symbol");
			}
			
			currTerm = currTerm.next;
		}
		
		if(term.paramSize() < 1)
		{
			error(e.line,e.col,"We do no support < Array Type > with No Entry");
		}
		return term;
	}
	
	Expression transExpr(OpExpr e)
	{
		// TODO: to support TimeStamp
		if(e.oper == OpExpr.AT)
		{
			error(e.line,e.col,"We do not support < TimeStamp >");
			return null;
		}
		
		Expression left=null, right=null;
		if(e.left != null) left = transExpr(e.left);
		if(e.right != null) right = transExpr(e.right);
		
		if(e.oper == OpExpr.SUB)
		{
			if((right instanceof Constant))
			{
				Constant var_right = (Constant) right;
				if(!var_right.isInteger())
				{
					error(e.line,e.col,"< Subscript > must be a Constant Integer!");
					return null;
				}
				if(left instanceof Variable)
				{
					Variable var_left = (Variable) left;;
					return new Variable(var_left.getSymbol() + "(" + var_right.getValue() + ")");
				} else
				{
					error(e.line,e.col,"Expression before < Subscript > must be a Symbol!");
					return null;
				}
			} else
			{
				error(e.line,e.col,"< Subscript > must be constant value!");
				return null;
			}
		}
		
		
		//////////////////////////////////////////////////////////////
		// Start Normal Operator From here
		/////////////////////////////////////////////////////////////
		OprTerm term = new OprTerm();
		if(left != null) term.addParam(left);
		if(right != null) term.addParam(right);
		switch (e.oper) {
		case OpExpr.PLUS: term.setOpr(OprTerm.PLUS); break;
		case OpExpr.MINUS: term.setOpr(OprTerm.MINUS); break;
		case OpExpr.MULT: term.setOpr(OprTerm.MUL); break;
		case OpExpr.DIV: term.setOpr(OprTerm.DIV); break;
		case OpExpr.MOD: term.setOpr(OprTerm.MOD); break;
		case OpExpr.POWER: term.setOpr(OprTerm.POWER); break;
		case OpExpr.EQ: term.setOpr(OprTerm.EQ); break;
		case OpExpr.NEQ: term.setOpr(OprTerm.NEQ); break;
		case OpExpr.LT: term.setOpr(OprTerm.LT); break;
		case OpExpr.LEQ: term.setOpr(OprTerm.LEQ); break;
		case OpExpr.GT: term.setOpr(OprTerm.GT); break;
		case OpExpr.GEQ: term.setOpr(OprTerm.GEQ); break;
		case OpExpr.AND: term.setOpr(OprTerm.AND); break;
		case OpExpr.OR: term.setOpr(OprTerm.OR); break;
		case OpExpr.IMPLY: 
			{// TODO: to support Imply operation : the boolean operation
				error(e.getLine(), e.getCol(),
					    "We Do Not Support Imply Operation : " + e.toString());
					return null;
			}
		case OpExpr.NOT: term.setOpr(OprTerm.NOT); break;
		default:
			error(e.getLine(), e.getCol(),
			    "The operation could not be applied : " + e.toString());
			return null;
		}
		return term;
	}
	
	Variable transExpr(SymbolExpr e) {
		return new Variable(e.name.toString());
	}
	
	FunctionCallTerm transExpr(FuncCallExpr e)
	{
		FunctionCallTerm term = new FunctionCallTerm(e.func.toString());
		if("Pred".equals(term.getFunc()))
		{
			// TODO: to support Time Stamp
			error(e.line, e.col, "We Do Not Support Function < Pred >");
			return term;
		}
		term.setParam(transExprList(e.args));
		return term;
	}
	
	Expression transExpr(NullExpr e)
	{
		return new Constant("null");
	}
	
	Expression transExpr(Expr e) {
		if (e instanceof DistributionExpr) {
			return transExpr((DistributionExpr) e);
		} else if (e instanceof BooleanExpr) {
			return transExpr((BooleanExpr) e);
		} else if (e instanceof DoubleExpr) {
			return transExpr((DoubleExpr) e);
		} else if (e instanceof IntExpr) {
			return transExpr((IntExpr) e);
		} else if (e instanceof StringExpr) {
			return transExpr((StringExpr) e);
		} else if (e instanceof NumberExpr) {
			return transExpr((NumberExpr) e);
		} else if (e instanceof ImplicitSetExpr) {
			// TODO: <set expression> generate more usage of set expression
			//       Currently only available for distribution UniformChoice({});
			//       Currently only support explicit set with Distinct Symbols
			return transExpr((ImplicitSetExpr) e);
		} else if (e instanceof ExplicitSetExpr) {
			return transExpr((ExplicitSetExpr) e);
		} else if (e instanceof TupleSetExpr) {
			
			error(e.line,e.col,"We do not support < Tuple Set Expression > in Figaro!");
			return null;
		} else if (e instanceof IfExpr) {
			return transExpr((IfExpr) e);
		} else if (e instanceof OpExpr) {
			return transExpr((OpExpr) e);
		} else if (e instanceof FuncCallExpr) {
			return transExpr((FuncCallExpr) e);
		} else if (e instanceof ListInitExpr) {
			//NOTE: We only support array containing CONSTANT
			return transExpr((ListInitExpr) e);
		} else if (e instanceof MapInitExpr) {
			return transExpr((MapInitExpr) e);
		} else if (e instanceof SymbolExpr) {
			return transExpr((SymbolExpr) e);
		} else if (e instanceof NullExpr) {
			return transExpr((NullExpr) e);
		} else if (e instanceof QuantifiedFormulaExpr) {
			error(e.line,e.col,"We do not support < Quantified Formula > in Figaro!");
			return null;
		}
		return null;
	}
	
	MappingTerm transExpr(MapInitExpr e)
	{
		MappingTerm term = new MappingTerm();
		
		ExprTupleList mapExprs = e.values;
		while (mapExprs != null) {
			term.addParam(transExpr(mapExprs.from), transExpr(mapExprs.to));
			mapExprs = mapExprs.next;
		}
		
		return term;
	}
	
	void transDec(Dec e) {
		if (e instanceof DistributionDec) {
			error(e.line,e.col,"We do not support < costumized distribution definition > in Figaro!");
		} else if (e instanceof FunctionDec) {
			transDec((FunctionDec) e);
		} else if (e instanceof NumberDec) {
			transDec((NumberDec) e);
		} else if (e instanceof ParameterDec) {
			// Do nothing
		}
	}
	
	// TODO: Add Restrictions for Class Names
	boolean illegalClassName(String name)
	{
		for(int i=0;i<keyWords.length;++i)
			if(keyWords[i].equals(name)) return true;
		return false;
	}
	
	void transDec(TypeDec e) {
		String name = e.name.toString();
		
		if(illegalClassName(name)){
			error(e.line,e.col,"Illegal Type Name < " + name + " >");
		}
		
		if (figaro.hasClass(name)) {
			error(e.line, e.col, "Type " + name + " already defined!");
		} else {
			figaro.addClass(name);
		}
	}
	
	String getNameType(Ty type) {
		String ret = "";
		if (type instanceof NameTy) {
			ret = ((NameTy) type).name.toString();
			// Basic Type
			if (figaro.basicBlogType(ret)) return figaro.convertType(ret);
			// A Class Type
			if (!figaro.hasClass(ret)) {
				error(type.line, type.col, "Type " + ret + " undefined!");
			}
		} else {
			error(type.line, type.col, "Type not allowed!");
		}
		return ret;
	}
	
	void transDec(DistinctSymbolDec e) {
		String type = getNameType(e.type);
		for (SymbolArrayList sa = e.symbols; sa != null; sa = sa.next) {
			if (sa.head == null) {
				error(sa.head.line, sa.head.col, "Symbol mistake!");
			} else {
				int sz = sa.head.size;
				String name = sa.head.name.toString();
				if(figaro.hasDistinctSymbol(name))
					error(sa.line,sa.col,"Duplicated variable <" + name + ">");
				else {
					figaro.addDistinctSymbol(name, type, sz);
					if(sz == 1)
					{
						figaro.addObject(name, type);
					} else
					{
						for(int k = 0; k < sz; ++ k)
							figaro.addObject(name + "(" + k + ")", type);
					}
				}
			}
		}
	}
	
	void transDec(NumberDec e) {
		String typ = getNameType(e.typ);
		
		if(!figaro.hasClass(typ))
			error(e.line,e.col,"No such type! < " + typ + " >");
		else {
		
		//TODO: <origin function> make e.params useful
		
			if(e.params != null)
				error(e.line,e.col,"We current do not support < origin function >!");
		
			figaro.setClassNumberStatement(typ, transExpr(e.body));
		}
	}
	
	String transType(Ty type)
	{
		if(type == null) return null;
		if (type instanceof NameTy) {
			return getNameType(type);
		}
		else if (type instanceof ArrayTy) {
			ArrayTy t = (ArrayTy) type;
			String x = getNameType(t.typ);
			int d = t.dim;
			String ret = "";
			for(int i=0;i<d;++i)
				ret = ret + "Array[";
			ret = ret + x;
			for(int i=0;i<d;++i)
				ret = ret + "]";
			return ret;
		}
		return null;
	}
	
	void transDec(FunctionDec e)
	{
		String type = transType(e.result);
		if(type == null) {
			error(e.line, e.col, "No valid type!");
			return ;
		}
		
		if (e instanceof OriginFuncDec)
		{
			//TODO: to support origin function
			error(e.line, e.col, "We do not support < Origin Function > now!");
			return ;
		}

		FigaroFunction func = new FigaroFunction(e.name.toString(), type, e instanceof RandomFuncDec);
		
		for (FieldList fl = e.params; fl != null; fl = fl.next)
		{
			String t = transType(fl.typ);
			if(t == null)
			{
				error(fl.line, fl.col, "Invalid Parameter Type");
				return ;
			}
			func.addParam(new Variable(fl.var.toString(), t));
		}
		if(e.body == null){
			error(e.line, e.col, "Function Body Cannot be Empty!");
			return ;
		}
		func.setExpr(transExpr(e.body));
		
		if(e instanceof RandomFuncDec)
		{
			if(func.paramSize() > 0)
			{
				if(func.paramSize() > 1)
				{
					error(e.line, e.col, "We Do Not support < Random Function > with 2 or more Parameters");
					return ;
				}
				if(!figaro.hasClass(func.getParam(0).getType()))
				{
					error(e.line, e.col, "We Do Not support < Random Function > with Non-Class Parameters");
				}
				func.setBelong(func.getParam(0).getType());
			}
		}
		figaro.addFunction(func);
	}
	
	void transEvi(EvidenceStmt e) {
		if (e instanceof ValueEvidence) {
			ValueEvidence evi = (ValueEvidence) e;
			
			figaro.addEvidence(new FigaroEvidence(transExpr(evi.left),transExpr(evi.right)));
		} else if (e instanceof SymbolEvidence) {
			error(e.line, e.col, "We Do Not Support < Evidence > of < Set Equivalence >");
		} else {
			error(e.line, e.col, "Unsupported Evidence type: " + e);
		}
	}
	
	void transQuery(QueryStmt e) {
		figaro.addQuery(new FigaroQuery(transExpr(e.query), e.toString()));
	}
	
	public boolean setup(Absyn e) {
		if (e instanceof StmtList) {
			StmtList stl = (StmtList) e;
			List<Dec> decStmts = new LinkedList<Dec>();
			List<EvidenceStmt> eviStmts = new LinkedList<EvidenceStmt>();
			List<QueryStmt> queryStmts = new LinkedList<QueryStmt>();
			for (; stl != null; stl = stl.next) 
				if (stl.head instanceof Dec) {
					decStmts.add((Dec) stl.head);
				} else if (stl.head instanceof EvidenceStmt) {
					eviStmts.add((EvidenceStmt) stl.head);
				} else if (stl.head instanceof QueryStmt) {
					queryStmts.add((QueryStmt) stl.head);
				} else 
					error(e.line,e.col,"No Such Statement Type");

			
			System.out.println("Checking Type Declar ....");
			
			// Generate All Different Types
			for(Stmt stmt : decStmts)
				if (stmt instanceof TypeDec)
					transDec((TypeDec) stmt);
			
			System.out.println(" >>> Type Declar Finished!");
			System.out.println("Checking Distinct Instances ....");
			
			// Generate All Distinct Instances
			for(Stmt stmt : decStmts)
				if (stmt instanceof DistinctSymbolDec)
					transDec((DistinctSymbolDec) stmt);
			
			System.out.println(" >>> Distinct Instances Finished!");
			System.out.println("Checking Number Statement ....");
			
			// Generate All Number Statements
			for(Stmt stmt : decStmts)
				if (stmt instanceof NumberDec)
					transDec((NumberDec) stmt);
			
			System.out.println(" >>> Number Statement Finished!");
			System.out.println("Checking Function Declar ....");
			
			// Generate All Functions
			for(Stmt stmt : decStmts)
				if(stmt instanceof FunctionDec)
					transDec((FunctionDec) stmt);
	
			System.out.println(" >>> Function Declar Finished!");
			System.out.println("Checking Evidence ....");
			
			// Evidence
			for(Stmt stmt : eviStmts)
				transEvi((EvidenceStmt)stmt);
			
			System.out.println(" >>> Evidence Finished!");
			System.out.println("Checking Query ....");
			
			// Query
			for(Stmt stmt : queryStmts)
				transQuery((QueryStmt)stmt);
				
			System.out.println(" >>> Query Finished!");
		} else {
			error(0, 0, "Invalid program");
		}
		
		
		if(!errorMsg.OK()) return false;
		
		if(!figaro.checkDAG(errorMsg)) {
			error(-1,-1,"Eventual DAG and Typing Check Failed! The Dependency is not Acyclic");
			return false;
		}
		
		return errorMsg.OK();
	}

	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}
}
