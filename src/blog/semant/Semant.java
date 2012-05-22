/**
 *
 */
package blog.semant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blog.Timestep;
import blog.absyn.*;
import blog.model.*;
import blog.msg.*;

/**
 * @author leili
 *
 */
public class Semant {

	private ErrorMsg errorMsg;
	private Model model;
	private Evidence evidence;
	private List<Query> queries;

	List<String> packages;

	public Semant(ErrorMsg msg) {
		this(new Model(), new Evidence(), new ArrayList<Query>(), msg);
	}

	public Semant(ModelEvidenceQueries meq, ErrorMsg msg) {
		this(meq.model, meq.evidence, meq.queries, msg);
	}

	public Semant(Model m, Evidence e, List<Query> qs, ErrorMsg msg) {
		model = m;
		evidence = e;
		errorMsg = msg;
		queries = qs;
		initialize();
	}

	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}

	/**
	 * search the pre-loaded packages for the classname of the distribution
	 * function
	 *
	 * @param classname
	 * @return
	 */
	Class getClassWithName(String classname) {
		for (String pkg : packages) {
			try {
				return Class.forName(pkg + classname);
			} catch (ClassNotFoundException e) {
				// continue loop
			}
		}
		return null;
	}

	protected boolean checkSymbolDup(int line, int col, String name) {
		if (getFunction(name, Collections.EMPTY_LIST) == null) {
			return true;
		} else {
			error(line, col, "Function/Symbol " + name
					+ " without argument has already been declared.");
			return false;
		}
	}

	protected Function getFunction(String name, List<Type> argTypeList) {
		Function f = model.getFunction(new Function.Sig(name, argTypeList));
		if ((f == null) && (evidence != null)) {
			f = evidence.getSkolemConstant(name);
		}
		return f;
	}

	Type getNameType(Ty type) {
		Type ty = null;
		if (type instanceof NameTy) {
			String name = ((NameTy) type).name.toString();
			ty = model.getType(name);
			if (ty == null) {
				error(type.line, type.col, "Type " + name + " undefined!");
			}
		} else {
			error(type.line, type.col, "Type not allowed!");
		}
		return ty;
	}

	/**
	 * check whether e is a list of symbol names (function call without argument)
	 *
	 * @param e
	 * @return a list of Symbol names
	 */
	List<String> getSymbolList(ExprList e) {
		List<String> res = new ArrayList<String>();
		for (; e != null; e = e.next) {
			Expr h = e.head;
			if (h instanceof FuncCallExpr) {
				FuncCallExpr fc = (FuncCallExpr) h;
				String fn = fc.func.toString();
				if (fc.args == null)
					checkSymbolDup(fc.line, fc.col, fn);
				else {
					error(fc.line, fc.col, "Invalid expression: expecting No argument");
				}
				res.add(fn);
			} else {
				error(h.line, h.col, "Invalid expression: expecting Symbol names");
			}
		}
		return res;
	}

	Type getType(Ty type) {
		if (type instanceof NameTy) {
			return getNameType(type);
		}
		// TODO
		return null;
	}

	/**
	 * create default library search packages for distribution function class
	 */
	protected void initialize() {
		packages = new ArrayList<String>();
		packages.add("");
		packages.add("blog.distrib.");
	}

	public void setPackages(List<String> pks) {
		packages = pks;
	}

	void transDec(Dec e) {
		if (e instanceof TypeDec) {
			transDec((TypeDec) e);
		} else if (e instanceof DistinctSymbolDec) {
			transDec((DistinctSymbolDec) e);
		} else if (e instanceof DistributionDec) {
			// TODO
		} else if (e instanceof FunctionDec) {
			transDec((FunctionDec) e);
		} else if (e instanceof NumberDec) {
			transDec((NumberDec) e);
		} else if (e instanceof ParameterDec) {
			// TODO
		}
	}

	/**
	 * translate the Distinct symbol declaration to internal model representation
	 *
	 * @param e
	 */
	void transDec(DistinctSymbolDec e) {
		Type type = getNameType(e.type);
		for (SymbolArrayList sa = e.symbols; sa != null; sa = sa.next) {
			if (sa.head == null) {
				error(sa.head.line, sa.head.col, "Symbol mistake!");
			} else {
				int sz = sa.head.size;
				String name = sa.head.name.toString();
				if (checkSymbolDup(sa.line, sa.col, name))
					if (sz == 1) {
						model.addEnumeratedObject(name, type);
					} else {
						for (int i = 1; i <= sz; i++) {
							model.addEnumeratedObject(name + i, type);
						}
					}
			}
		}
	}

	/**
	 * translate Function declaration to internal representation
	 *
	 * @param e
	 */
	void transDec(FunctionDec e) {
		Type resTy = getType(e.result);
		List<Type> argTy = new ArrayList<Type>();
		List<String> argVars = new ArrayList<String>();
		for (FieldList fl = e.params; fl != null; fl = fl.next) {
			Type ty = getType(fl.typ);
			argTy.add(ty);
			if (fl.var != null) {
				String vn = fl.var.toString();
				if (argVars.contains(vn)) {
					error(fl.line, fl.col, "Variable " + vn + " used multiple times");
				} else {
					argVars.add(vn);
				}
			}
		}

		String name = e.name.toString();
		Function fun = getFunction(name, argTy);

		if (fun != null) {
			error(e.line, e.col, "Function " + name + " already defined");
		}

		if (!(model.getOverlappingFuncs(name,
				(Type[]) argTy.toArray(new Type[argTy.size()])).isEmpty())) {
			error(e.line, e.col, "Function " + name + " overlapped");
		}

		if (e instanceof FixedFuncDec) {
			// TODO
		} else if (e instanceof RandomFuncDec) {
			DependencyModel dm;
			dm = transDependency(e.body, resTy, resTy.getDefaultValue());
			RandomFunction f = new RandomFunction(name, argTy, resTy, dm);
			f.setArgVars(argVars);
			fun = f;
		} else if (e instanceof OriginFuncDec) {
			if (argTy.size() != 1) {
				error(e.line, e.col,
						"Incorrect number of arguments: origin function expecting exactly One argument");
			}
			if (e.body != null) {
				error(
						e.line,
						e.col,
						"Invalid origin function definition: the body of origin functions should be empty");
			}
			OriginFunction f = new OriginFunction(name, argTy, resTy);
			fun = f;
		}
		model.addFunction(fun);
	}

	DependencyModel transDependency(Expr e, Type resTy, Object defVal) {
		Object body = transExpr(e);
		List<Clause> cl = new ArrayList<Clause>(1);
		if (body instanceof Clause) {
			cl.add((Clause) body);
		} else if (e instanceof IfExpr) {
			cl = transExpr((IfExpr) e);
		} else {
			error(e.line, e.col, "invalid body of dependency clause");
		}
		return new DependencyModel(cl, resTy, defVal);
	}

	/**
	 * semantic checking for evidence statement and translate to internal
	 * representation
	 *
	 * @param e
	 */
	void transEvi(EvidenceStmt e) {
		if (e instanceof ValueEvidence) {
			transEvi((ValueEvidence) e);
		} else {
			error(e.line, e.col, "Unsupported Evidence type: " + e);
		}
	}

	/**
	 * valid evidence format include (will be checked in semantic checking)
	 *
	 * - general form: random expression = fixed expression - symbol evidence:
	 * implicit_set = explicit_set of ids - number_evidence: # implicit_set = int
	 * constant
	 *
	 * @param e
	 */
	void transEvi(ValueEvidence e) {
		Object left = transExpr(e.left);

		if (left instanceof CardinalitySpec) {
			// number evidence
			// # implicit_set = int constant
			ArgSpec value = null;
			if (e.right instanceof IntExpr) {
				// ok
				value = (ArgSpec) transExpr(e.right);
			} else {
				error(e.right.line, e.right.col,
						"Number evidence expecting integer(natural number) on the right side");
			}
			evidence.addValueEvidence(new ValueEvidenceStatement(
					(CardinalitySpec) left, value));
		} else if (left instanceof ImplicitSetSpec) {
			// symbol evidence
			// implicit set = set of ids
			List<String> value = null;
			if (e.right instanceof ExplicitSetExpr) {
				// ok
				value = getSymbolList(((ExplicitSetExpr) e.right).values);
			} else {
				error(
						e.right.line,
						e.right.col,
						"Invalid expression in right side of symbol evidence: explicit set of symbols expected");
			}
			SymbolEvidenceStatement sevid = new SymbolEvidenceStatement(
					(ImplicitSetSpec) left, value);
			if (!evidence.addSymbolEvidence(sevid)) {
				error(e.right.line, e.right.col, "Duplicate names in symbol evidence.");
			}
			for (SkolemConstant obj : sevid.getSkolemConstants()) {
				model.addFunction(obj);
			}

			// ValueEvidenceStatement vst = new ValueEvidenceStatement();
		} else if (left instanceof ArgSpec) {
			// general value expression
			Object value = transExpr(e.right);
			if (value instanceof ArgSpec) {
				// need more semantic checking on type match
				evidence.addValueEvidence(new ValueEvidenceStatement((ArgSpec) left,
						(ArgSpec) value));
			} else {
				error(e.right.line, e.right.col,
						"Invalid expression on the right side of evidence.");
			}
		} else {
			error(e.left.line, e.left.col,
					"Invalid expression on the left side of evidence.");
		}
	}

	/**
	 * translate number statement to model representation
	 *
	 * @param e
	 */
	void transDec(NumberDec e) {
		Type typ = getNameType(e.typ);

		List<OriginFunction> fs = new ArrayList<OriginFunction>();
		List<String> argVars = new ArrayList<String>();
		for (OriginFieldList fl = e.params; fl != null; fl = fl.next) {
			String name = fl.func.toString();
			Function f = getFunction(name, Collections.singletonList(typ));
			if (f == null) {
				error(fl.line, fl.col, "function undefined: " + name);
			} else if (!(f instanceof OriginFunction)) {
				error(fl.line, fl.col, "Function " + name + " with argument type "
						+ typ.getName() + " has not been declared as an origin function.");
			} else if (fs.contains(f)) {
				error(fl.line, fl.col, "Origin function " + name
						+ " used more than once");
			} else {
				fs.add((OriginFunction) f);
			}
			String vn = fl.var.toString();
			if (argVars.contains(vn)) {
				error(fl.line, fl.col, "Variable " + vn + " used multiple times");
			} else {
				argVars.add(vn);
			}
		}

		POP pop = new POP(typ, fs, transDependency(e.body,
				BuiltInTypes.NATURAL_NUM, new Integer(0)));
		if (typ.getPOPWithOriginFuncs(pop.getOriginFuncSet()) != null) {
			error(e.line, e.col, "number statement #" + typ.getName()
					+ " uses same origin functions as earlier number statement.");
		} else {
			typ.addPOP(pop);
		}
		pop.setGenObjVars(argVars);

	}

	/**
	 * add the declared type to model
	 *
	 * @param e
	 */
	void transDec(TypeDec e) {
		String name = e.name.toString();
		if (model.getType(name) != null) {
			error(e.line, e.col, "Type " + name + " already defined!");
		} else {
			model.addType(name);
		}
	}

	Clause transExpr(DistributionExpr e) {
		Class cls = getClassWithName(e.name.toString());
		if (cls == null) {
			error(e.line, e.col, "Class not found: " + e.name);
		}

		List<ArgSpec> as = null;
		if (e.args != null) {
			as = transExprList(e.args, true);
		}
		// TODO leili: check whether correct
		return new Clause(TrueFormula.TRUE, cls, as, Collections.EMPTY_LIST);
	}

	ArgSpec transExpr(DoubleExpr e) {
		// TODO is there a better way than using function?
		Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
				String.valueOf(e.value), BuiltInTypes.REAL, e.value),
				Collections.EMPTY_LIST);
		t.setLocation(e.line);
		return t;
	}

	Object transExpr(Expr e) {
		if (e instanceof DistributionExpr) {
			return transExpr((DistributionExpr) e);
		} else if (e instanceof DoubleExpr) {
			return transExpr((DoubleExpr) e);
		} else if (e instanceof IntExpr) {
			return transExpr((IntExpr) e);
		} else if (e instanceof NumberExpr) {
			return transExpr((NumberExpr) e);
		} else if (e instanceof ImplicitSetExpr) {
			return transExpr((ImplicitSetExpr) e);
		} else if (e instanceof ExplicitSetExpr) {
			return transExpr((ExplicitSetExpr) e);
		} else if (e instanceof IfExpr) {
			return transExpr((IfExpr) e);
		} else if (e instanceof OpExpr) {
			return transExpr((OpExpr) e);
		} else if (e instanceof FuncCallExpr) {
			return transExpr((FuncCallExpr) e);
		} else if (e instanceof MapInitExpr) {
			return transExpr((MapInitExpr) e);
		} else if (e instanceof SymbolExpr) {
			return transExpr((SymbolExpr) e);
		}

		return null;
	}

	ExplicitSetSpec transExpr(ExplicitSetExpr e) {
		// TODO
		return null;
	}

	ArgSpec transExpr(SymbolExpr e) {
		Term t = new SymbolTerm(e.name.toString());
		t.setLocation(e.line);
		return t;
	}

	ArgSpec transExpr(FuncCallExpr e){
		List<ArgSpec> args = transExprList(e.args, true);
		Term t = new FuncAppTerm(e.func.toString(), args);
		t.setLocation(e.line);
		return t;
	}

	List<ArgSpec> transExpr(MapInitExpr e) {
		// TODO: This is incorrect. This code is only to make
		// the poission-ball case work
		ArrayList<ArgSpec> vals = new ArrayList<ArgSpec>();
		ExprTupleList current = e.values;
		while (current != null) {
			vals.add((ArgSpec) transExpr(current.to));
			current = current.next;
		}
		return vals;
	}

	List<Clause> transExpr(IfExpr e) {
		ArrayList<Clause> clauses = new ArrayList<Clause>();

		// TODO: add proper check, error if not
		Formula test = (Formula) transExpr(e.test);

		Expr thenClause = e.thenclause;
		if (thenClause instanceof DistributionExpr) {
			DistributionExpr distExpr = (DistributionExpr) thenClause;
			Class cls = getClassWithName(distExpr.name.toString());
			if (cls == null) {
				error(distExpr.line, distExpr.col, "Class not found: " + distExpr.name);
			}

			List<ArgSpec> as = null;
			if (distExpr.args != null) {
				as = transExprList(distExpr.args, true);
			}
			clauses.add(new Clause(test, cls, as, Collections.EMPTY_LIST));
		}

		Expr elseClause = e.elseclause;
		if (elseClause instanceof IfExpr) {
			List<Clause> rest = transExpr((IfExpr) elseClause);
			clauses.addAll(rest);
		} else if (elseClause instanceof DistributionExpr) {
			clauses.add( transExpr((DistributionExpr) elseClause ));
		}
		return clauses;
	}

	ArgSpec transExpr(IntExpr e) {
		// TODO
		Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
				String.valueOf(e.value), BuiltInTypes.INTEGER, e.value),
				Collections.EMPTY_LIST);
		t.setLocation(e.line);
		return t;
	}

	ImplicitSetSpec transExpr(ImplicitSetExpr e) {
		Type typ = getNameType(e.typ);
		String vn;
		if (e.var != null) {
			vn = e.var.toString();
		} else {
			vn = "_";
		}
		Formula cond = TrueFormula.TRUE;
		if (e.cond != null) {
			Object c = transExpr(e.cond);
			if (c instanceof Formula) {
				cond = (Formula) c;
			} else {
				error(
						e.cond.line,
						e.cond.col,
						"Invalid expression as condition in implicit set: formula(boolean valued expression) expected");
			}
		}
		return new ImplicitSetSpec(vn, typ, cond);
	}

	/**
	 * number expression translated to CardinalitySpec
	 *
	 * @param e
	 * @return
	 */
	CardinalitySpec transExpr(NumberExpr e) {
		Object r = transExpr(e.values);
		if (r instanceof ImplicitSetSpec) {
			return new CardinalitySpec((ImplicitSetSpec) r);
		} else {
			error(e.line, e.col, "Number expression expecting implicit set");
		}
		return null;
	}

	ArgSpec transExpr(OpExpr e) {
		Object left, right;
		switch (e.oper) {
		case OpExpr.PLUS:
			break;
		case OpExpr.MINUS:
			break;
		case OpExpr.MULT:
			break;
		case OpExpr.DIV:
			break;
		case OpExpr.MOD:
			break;
		case OpExpr.EQ:
			left = transExpr(e.left);
			right = transExpr(e.right);
			return new EqualityFormula((Term) left, (Term) right);
		case OpExpr.NEQ:
			left = transExpr(e.left);
			right = transExpr(e.right);
			return new NegFormula(new EqualityFormula((Term) left, (Term)right));
		case OpExpr.LT:
			break;
		case OpExpr.LEQ:
			break;
		case OpExpr.GT:
			break;
		case OpExpr.GEQ:
			break;
		case OpExpr.AND:
			left = transExpr(e.left);
			right = transExpr(e.right);
			return new ConjFormula((Formula) left, (Formula) right);
		case OpExpr.OR:
			break;
		case OpExpr.NOT:
			break;
		case OpExpr.SUB:
			break;
		case OpExpr.AT:
			if (e.left == null && e.right instanceof IntExpr)
				BuiltInFunctions.getLiteral(e.toString(), BuiltInTypes.TIMESTEP,
						Timestep.at(((IntExpr) e.right).value));
			break;
		default:
			error(e.getLine(), e.getCol(),
					"The operation could not be applied" + e.toString());
		}
		return null;
	}

	/**
	 * check list of expressions
	 *
	 * @param e
	 *          list of expression
	 * @param allowRandom
	 *          whether allow terms with random functions in the expression
	 * @return
	 */
	List<ArgSpec> transExprList(ExprList e, boolean allowRandom) {
		List<ArgSpec> args = new ArrayList<ArgSpec>();
		for (; e != null; e = e.next) {
			Object o = transExpr(e.head);
			if (o instanceof List)
				return (List<ArgSpec>) o;
			args.add((ArgSpec) o);
		}
		// TODO add checking for allowRandom

		return args;
	}

	/**
	 * @param e
	 */
	void transQuery(QueryStmt e) {
		// TODO Auto-generated method stub
		Object as = transExpr(e.query);
		Query q;
		if (as != null) {
			if (as instanceof Formula) {
				q = new FormulaQuery((Formula) as);
			} else {
				q = new ArgSpecQuery((ArgSpec) as);
			}

			queries.add(q);
		}

	}

	void transStmt(blog.absyn.Stmt e) {
		if (e instanceof Dec) {
			transDec((Dec) e);
		} else if (e instanceof EvidenceStmt) {
			transEvi((EvidenceStmt) e);
		} else if (e instanceof QueryStmt) {
			transQuery((QueryStmt) e);
		}
	}

	void transStmtList(StmtList e) {
		for (; e != null; e = e.next) {
			transStmt(e.head);
		}
	}

	/**
	 * semantic checking and translate the BLOG program to model representation
	 *
	 * @param e
	 * @return whether any error happened during parsing and translating
	 */
	public boolean transProg(Absyn e) {
		if (e instanceof StmtList) {
			transStmtList((StmtList) e);
		} else {
			error(0, 0, "Invalid program");
		}
		return errorMsg.OK();
	}

	public ModelEvidenceQueries getModelEvidenceQueries() {
		return new ModelEvidenceQueries(model, evidence, queries);
	}

}
