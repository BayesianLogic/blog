/**
 * 
 */
package blog.semant;

import blog.absyn.Dec;
import blog.absyn.Expr;
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
		}
	}
	
	void transDec(TypeDec e) {
		String name = e.name.toString();
		if (model.getType(name) != null) {
			error(e.line, e.col, "Type " + name +  " already defined!");
		} else {
			model.addType(name);
		}
	}
	
	void transExpr(Expr e) {
		
	}
	
	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}
	
	
}
