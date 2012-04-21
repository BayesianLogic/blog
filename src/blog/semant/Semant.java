/**
 * 
 */
package blog.semant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blog.absyn.Dec;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DistributionDec;
import blog.absyn.DistributionExpr;
import blog.absyn.DoubleExpr;
import blog.absyn.EvidenceStmt;
import blog.absyn.Expr;
import blog.absyn.ExprList;
import blog.absyn.FieldList;
import blog.absyn.FixedFuncDec;
import blog.absyn.FunctionDec;
import blog.absyn.ImplicitSetExpr;
import blog.absyn.IntExpr;
import blog.absyn.NameTy;
import blog.absyn.NumberDec;
import blog.absyn.NumberExpr;
import blog.absyn.OriginFieldList;
import blog.absyn.RandomFuncDec;
import blog.absyn.SymbolArrayList;
import blog.absyn.Ty;
import blog.absyn.TypeDec;
import blog.absyn.ValueEvidence;
import blog.model.ArgSpec;
import blog.model.BuiltInFunctions;
import blog.model.BuiltInTypes;
import blog.model.CardinalitySpec;
import blog.model.Clause;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.Model;
import blog.model.OriginFunction;
import blog.model.POP;
import blog.model.RandomFunction;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.model.ValueEvidenceStatement;
import blog.msg.ErrorMsg;

/**
 * @author leili
 * 
 */
public class Semant {

	private ErrorMsg errorMsg;
	private Model model;
	private Evidence evidence;

	List<String> packages;

	public Semant(ErrorMsg msg) {
		this(new Model(), new Evidence(), msg);
	}

	public Semant(Model m, Evidence e, ErrorMsg msg) {
		model = m;
		evidence = e;
		errorMsg = msg;
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

	private Function getFunction(String name, List<Type> argTypeList) {
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
					error(fl.line, fl.pos, "Variable " + vn + " used multiple times");
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

		if (model.getOverlappingFuncs(name, (Type[]) argTy.toArray()) != null) {
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
		}
		model.addFunction(fun);
	}

	DependencyModel transDependency(Expr e, Type resTy, Object defVal) {
		Object body = transExpr(e);
		List<Clause> cl = new ArrayList<Clause>(1);
		if (body instanceof Clause) {
			cl.add((Clause) body);
		} else {
			error(e.line, e.col, "invalid body of dependency clause");
		}
		return new DependencyModel(cl, resTy, defVal);
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
				error(fl.line, fl.pos, "function undefined: " + name);
			} else if (!(f instanceof OriginFunction)) {
				error(fl.line, fl.pos, "Function " + name + " with argument type "
						+ typ.getName() + " has not been declared as an origin function.");
			} else if (fs.contains(f)) {
				error(fl.line, fl.pos, "Origin function " + name
						+ " used more than once");
			} else {
				fs.add((OriginFunction) f);
			}
			String vn = fl.var.toString();
			if (argVars.contains(vn)) {
				error(fl.line, fl.pos, "Variable " + vn + " used multiple times");
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
		// TODO
		Class cls = getClassWithName(e.name.toString());
		if (cls == null) {
			error(e.line, e.col, "Class not found: " + e.name);
		}

		List<ArgSpec> as = null;
		if (e.args != null) {
			as = transExprList(e.args, true);
		}
		return new Clause(TrueFormula.TRUE, cls, as, null);
	}

	ArgSpec transExpr(DoubleExpr e) {
		// TODO
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
		}

		return null;
	}

	ArgSpec transExpr(IntExpr e) {
		// TODO
		Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
				String.valueOf(e.value), BuiltInTypes.INTEGER, e.value),
				Collections.EMPTY_LIST);
		t.setLocation(e.line);
		return t;
	}
	
	CardinalitySpec transExpr(NumberExpr e) {
		// TODO
		
		return null;
	}

	List<ArgSpec> transExprList(ExprList e, boolean allowRandom) {
		List<ArgSpec> args = new ArrayList<ArgSpec>();
		for (; e != null; e = e.next) {
			args.add((ArgSpec) transExpr(e.head));
		}
		return args;
	}

	void transStmt(blog.absyn.Stmt e) {
		if (e instanceof Dec) {
			transDec((Dec) e);
		} if (e instanceof EvidenceStmt) {
			transEvi((EvidenceStmt) e);
		}
	}

	/**
	 * semantic checking for evidence statement and translate to internal representation
	 * @param e
	 */
	void transEvi(EvidenceStmt e) {
		if (e instanceof ValueEvidence) {
			transEvi((ValueEvidence) e);
		}
		// TODO if more evidence
	}
	
	void transEvi(ValueEvidence e) {
//		Object left = transExpr(e.left);
//		Object right = transExpr(e.right);
		
		if (e.left instanceof NumberExpr) {
			// number evidence
			// # implicit_set = int constant
			NumberExpr le = (NumberExpr) e.left;
			CardinalitySpec cs = null;
			
			if (le.values instanceof ImplicitSetExpr) {
				cs = transExpr(le);
			} else {
				error(le.line, le.col, "Number evidence expecting implicit set on the left side");
			}
			if (e.right instanceof IntExpr) {
				// good
				
			} else {
				error(e.right.line, e.right.col, "Number evidence expecting integer(natural number) on the right side");
			}
			
			ArgSpec value = (ArgSpec) transExpr(e.right);
			
			
			evidence.addValueEvidence(new ValueEvidenceStatement(cs, value));
			
		}
		//TODO
	}

}
