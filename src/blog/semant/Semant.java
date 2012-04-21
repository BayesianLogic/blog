/**
 * 
 */
package blog.semant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import blog.FuncAppTerm;
import blog.absyn.Dec;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DistributionDec;
import blog.absyn.DistributionExpr;
import blog.absyn.DoubleExpr;
import blog.absyn.Expr;
import blog.absyn.ExprList;
import blog.absyn.FieldList;
import blog.absyn.FixedFuncDec;
import blog.absyn.FunctionDec;
import blog.absyn.IntExpr;
import blog.absyn.NameTy;
import blog.absyn.RandomFuncDec;
import blog.absyn.SymbolArrayList;
import blog.absyn.Ty;
import blog.absyn.TypeDec;
import blog.model.ArgSpec;
import blog.model.BuiltInFunctions;
import blog.model.BuiltInTypes;
import blog.model.Clause;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.Function;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.Type;
import blog.msg.ErrorMsg;

/**
 * @author leili
 * 
 */
public class Semant {

	private ErrorMsg errorMsg;
	private Model model;
	private Evidence evidence;

	public Semant(ErrorMsg msg) {
		this(new Model(), new Evidence(), msg);
	}

	public Semant(Model m, Evidence e, ErrorMsg msg) {
		model = m;
		evidence = e;
		errorMsg = msg;
	}

	/**
	 * create default library search packages for distribution function class
	 */
	protected void initialize() {
		packages = new ArrayList<String>();
		packages.add("");
		packages.add("blog.distrib.");
	}

	void transStmt(blog.absyn.Stmt e) {
		if (e instanceof Dec) {
			transDec((Dec) e);
		}
	}

	void transDec(Dec e) {
		if (e instanceof TypeDec) {
			transDec((TypeDec) e);
		} else if (e instanceof DistinctSymbolDec) {
			transDec((DistinctSymbolDec) e);
		} else if (e instanceof DistributionDec) {
			// TO-DO
		} else if (e instanceof FunctionDec) {
			transDec((FunctionDec) e);
		}
	}

	/**
	 * translate fixed function to model representation
	 * 
	 * @param e
	 */
	void transDec(FixedFuncDec e) {
		Type type = getType(e.result);
		// TO-DO
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
			if (fl.name != null) {
				argVars.add(fl.name.toString());
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
			// TO-DO
		} else if (e instanceof RandomFuncDec) {
			// TO-DO
			Object body = transExpr(e.body);
			DependencyModel dm;
			List<Clause> cl = new ArrayList<Clause>(1);
			if (body instanceof Clause) {
				cl.add((Clause) body);
			} else {
				error(e.line, e.col, "invalid body of random function");
			}
			dm = new DependencyModel(cl, resTy, resTy.getDefaultValue());
			RandomFunction f = new RandomFunction(name, argTy, resTy, dm);
			f.setArgVars(argVars);
		}

	}

	public Function getFunction(String name, List<Type> argTypeList) {
		Function f = model.getFunction(new Function.Sig(name, argTypeList));
		if ((f == null) && (evidence != null)) {
			f = evidence.getSkolemConstant(name);
		}
		return f;
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
		// TO-DO
		return null;
	}

	Object transExpr(Expr e) {
		if (e instanceof DistributionExpr) {
			return transExpr((DistributionExpr) e);
		} else if (e instanceof DoubleExpr) {
			return transExpr((DoubleExpr) e);
		} else if (e instanceof IntExpr) {
			return transExpr((IntExpr) e);
		}

		return null;
	}

	Clause transExpr(DistributionExpr e) {
		// TO-DO
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
		// TO-DO
		Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
				String.valueOf(e.value), BuiltInTypes.REAL, e.value),
				Collections.EMPTY_LIST);
		t.setLocation(e.line);
		return t;
	}

	ArgSpec transExpr(IntExpr e) {
		// TO-DO
		Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
				String.valueOf(e.value), BuiltInTypes.INTEGER, e.value),
				Collections.EMPTY_LIST);
		t.setLocation(e.line);
		return t;
	}

	List<ArgSpec> transExprList(ExprList e, boolean allowRandom) {
		List<ArgSpec> args = new ArrayList<ArgSpec>();
		for (; e != null; e = e.next) {
			args.add((ArgSpec) transExpr(e.head));
		}
		return args;
	}

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

	public void setPackages(List<String> pks) {
		packages = pks;
	}

	List<String> packages;

	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}

}
