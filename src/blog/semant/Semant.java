/**
 * 
 */
package blog.semant;

import java.util.ArrayList;
import java.util.List;

import blog.absyn.Dec;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DistributionDec;
import blog.absyn.Expr;
import blog.absyn.FieldList;
import blog.absyn.FixedFuncDec;
import blog.absyn.NameTy;
import blog.absyn.RandomFuncDec;
import blog.absyn.SymbolArrayList;
import blog.absyn.Ty;
import blog.absyn.TypeDec;
import blog.model.Evidence;
import blog.model.Function;
import blog.model.Model;
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
		model = new Model();
		evidence = new Evidence();
	}

	public Semant(Model m, Evidence e) {
		model = m;
		evidence = e;
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
		} else if (e instanceof FixedFuncDec) {
			transDec((FixedFuncDec) e);
		} else if (e instanceof RandomFuncDec) {
			transDec((RandomFuncDec) e);
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

	void transDec(RandomFuncDec e) {
		Type resTy = getType(e.result);
		List<Type> argTy = new ArrayList<Type>();
		for (FieldList fl = e.params; fl != null; fl = fl.next) {
			Type ty = getType(fl.typ);
			argTy.add(ty);
		}

		String name = e.name.toString();
		Function fun = getFunction(name, argTy);

		if (fun != null) {
			error(e.line, e.col, "Function " + name + " already defined");
		}

		if (model.getOverlappingFuncs(name, (Type[]) argTy.toArray()) != null) {
			error(e.line, e.col, "Function " + name + " overlapped");
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

	void transExpr(Expr e) {

	}

	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}

}
