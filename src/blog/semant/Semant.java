/**
 * 
 */
package blog.semant;

import blog.absyn.Dec;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DistributionDec;
import blog.absyn.Expr;
import blog.absyn.FixedFuncDec;
import blog.absyn.NameTy;
import blog.absyn.SymbolArrayList;
import blog.absyn.Ty;
import blog.absyn.TypeDec;
import blog.model.*;
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
			//TO-DO
		} else if (e instanceof FixedFuncDec) {
			transDec((FixedFuncDec) e);
		}
	}
	
	void transDec(FixedFuncDec e) {
		Type type = getType(e.result);
		
	}
	
	
	
	/**
	 * add the declared type to model
	 * @param e
	 */
	void transDec(TypeDec e) {
		String name = e.name.toString();
		if (model.getType(name) != null) {
			error(e.line, e.col, "Type " + name +  " already defined!");
		} else {
			model.addType(name);
		}
	}
	
	
	/**
	 * translate the Distinct symbol declaration to internal model representation
	 * @param e
	 */
	void transDec(DistinctSymbolDec e) {
			Type type = getNameType(e.type);			
			for (SymbolArrayList sa=e.symbols; sa != null; sa = sa.next) {
				if (sa.head == null) {
					error(sa.head.line, sa.head.col, "Symbol mistake!");
				} else {
					int sz = sa.head.size;
					String name = sa.head.name.toString();
					if (sz == 1) {
						model.addEnumeratedObject(name, type);
					} else {
						for (int i=1; i <= sz; i++) {
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
			error(type.line, type.col, "Type " + name +  " undefined!");
		}
		}
		else {
			error(type.line, type.col, "Type not allowed!");			
		}
		return ty;
	}
	
	Type getType(Ty type) {
//	TO-DO
		return null;
	}
	
	
	
	
	
	
	void transExpr(Expr e) {
		
	}
	
	
	
	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}
	
	
}
