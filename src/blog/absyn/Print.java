package blog.absyn;

public class Print {

  java.io.PrintStream out;

  public Print(java.io.PrintStream o) {out=o;}

  void indent(int d) {
      for(int i=0; i<d; i++)
            out.print(' ');
  }

  void say(String s) {
            out.print(s);
  }

  void say(int i) {
    Integer local = new Integer(i);
    out.print(local.toString());
  }

  void say(boolean b) {
    Boolean local = new Boolean(b);
    out.print(local.toString());
  }

  void sayln(String s) {
	say(s); say("\n");
  }

  void prExp(OpExpr e, int d) {
  	if (e==null) return;
    sayln("OpExpr(");
    indent(d+1);
    switch(e.oper) {
    case OpExpr.PLUS: say("PLUS"); break;
    case OpExpr.MINUS: say("MINUS"); break;
    case OpExpr.MULT: say("MULT"); break;
    case OpExpr.DIV: say("DIV"); break;
    case OpExpr.MOD: say("MOD"); break;
    case OpExpr.EQ: say("EQ"); break;
    case OpExpr.NEQ: say("NEQ"); break;
    case OpExpr.LT: say("LT"); break;
    case OpExpr.LEQ: say("LEQ"); break;
    case OpExpr.GT: say("GT"); break;
    case OpExpr.GEQ: say("GEQ"); break;
    case OpExpr.AND: say("AND"); break;
    case OpExpr.OR: say("OR"); break;
    case OpExpr.NOT: say("NOT"); break;
    case OpExpr.SUB: say("SUB"); break;
    default:
      throw new Error("Print.prExp.OpExpr");
    }
    sayln(",");
    prExp(e.left, d+1); sayln(",");
    prExp(e.right, d+1); say(")");
  }

  void prExp(NullExpr e, int d) {
    say("NullExpr()");
  }

  void prExp(IntExpr e, int d) {
    say("IntExpr("); say(e.value); say(")");
  }

  void prExp(StringExpr e, int d) {
    say("StringExpr("); say(e.value); say(")");
  }

  void prExp(FuncCallExpr e, int d) {
    say("FuncCallExpr("); say(e.func.toString()); sayln(",");
    prExplist(e.args, d+1); say(")");
  }

  void prExp(IfExpr e, int d) {
    sayln("IfExpr(");
    prExp(e.test, d+1); sayln(",");
    prExp(e.thenclause, d+1);
    if (e.elseclause!=null) { /* else is optional */
      sayln(",");
      prExp(e.elseclause, d+1);
    }
    say(")");
  }

  void prExp(ListInitExpr e, int d) {
    say("ListInitExpr("); 
    prExplist(e.values, d+1); 
    say(")");
  }
  
  void prExp(ExplicitSetExpr e, int d) {
  	say("ExplicitSetExpr(");
  	prExplist(e.values, d+1);
  	say(")");
  }

  void prExp(ImplicitSetExpr e, int d) {
  	say("ExplicitSetExpr(");
  	prTy(e.type, d+1);
  	sayln(",");
  	prSymbol(e.var, d+1);
  	sayln(",");
  	prExp(e.cond, d+1);
  	prExplist(e.values, d+1);
  	say(")");
  }
  
  /* Print Exp class types. Indent d spaces. */
  public void prExp(Expr e, int d) {
    indent(d);
    if (e instanceof OpExpr) prExp((OpExpr)e, d);
    else if (e instanceof NullExpr) prExp((NullExpr) e, d);
    else if (e instanceof IntExpr) prExp((IntExpr) e, d);
    else if (e instanceof StringExpr) prExp((StringExpr) e, d);
    else if (e instanceof BooleanExpr) prExp((BooleanExpr) e, d);
    else if (e instanceof FuncCallExpr) prExp((FuncCallExpr) e, d);
    else if (e instanceof IfExpr) prExp((IfExpr) e, d);
    else if (e instanceof ListInitExpr) prExp((ListInitExpr) e, d);
    else if (e instanceof ExplicitSetExpr) prExp((ExplicitSetExpr) e, d);
    else if (e instanceof ImplicitSetExpr) prExp((ImplicitSetExpr) e, d);
    else throw new Error("Print.prExp");
  }
  
  

  void prDec(FunctionDec d, int i) {
    say("FunctionDec(");
    if (d!=null) {
      sayln(d.name.toString());
      prFieldlist(d.params, i+1); sayln(",");
      if (d.result!=null) {
	indent(i+1); sayln(d.result.name.toString());
      }
      prExp(d.body, i+1); sayln(",");
      indent(i+1); prDec(d.next, i+1);
    }
    say(")");
  }

  void prDec(VarDec d, int i) {
    say("VarDec("); say(d.name.toString()); sayln(",");
    if (d.typ!=null) {
      indent(i+1); say(d.typ.name.toString());  sayln(",");
    }
    prExp(d.init, i+1); sayln(",");
    indent(i+1); say(d.escape); say(")");
  }

  void prDec(TypeDec d, int i) {
    if (d!=null) {
      say("TypeDec("); say(d.name.toString()); sayln(",");
      prTy(d.ty, i+1);
      if (d.next!=null) {
	say(","); prDec(d.next, i+1);
      }
      say(")");
    }
  }

  void prDec(Dec d, int i) {
    indent(i);
    if (d instanceof FunctionDec) prDec((FunctionDec) d, i);
    else if (d instanceof VarDec) prDec((VarDec) d, i);
    else if (d instanceof TypeDec) prDec((TypeDec) d, i);
    else throw new Error("Print.prDec");
  }

  void prTy(NameTy t, int i) {
    say("NameTy("); say(t.name.toString()); say(")");
  }

  void prTy(RecordTy t, int i) {
    sayln("RecordTy(");
    prFieldlist(t.fields, i+1); say(")");
  }

  void prTy(ArrayTy t, int i) {
    say("ArrayTy("); say(t.typ.toString()); say(")");
  }

  void prTy(Ty t, int i) {
    if (t!=null) {
      indent(i);
      if (t instanceof NameTy) prTy((NameTy) t, i);
      else if (t instanceof RecordTy) prTy((RecordTy) t, i);
      else if (t instanceof ArrayTy) prTy((ArrayTy) t, i);
      else throw new Error("Print.prTy");
    }
  }

  void prFieldlist(FieldList f, int d) {
    indent(d);
    say("Fieldlist(");
    if (f!=null) {
      sayln("");
      indent(d+1); say(f.name.toString()); sayln("");
      indent(d+1); say(f.typ.toString()); sayln(",");
      indent(d+1); say(f.escape);
      sayln(",");
      prFieldlist(f.next, d+1);
    }
    say(")");
  }

  void prExplist(ExprList e, int d) {
    indent(d);
    say("ExpList(");
    if (e!=null) {
      sayln("");
      prExp(e.head, d+1);
      if (e.next != null) {
	sayln(","); prExplist(e.next, d+1);
      }
    }
    say(")");
  }

  void prDecList(DecList v, int d) {
    indent(d);
    say("DecList(");
    if (v!=null) {
      sayln("");
      prDec(v.head, d+1); sayln(",");
      prDecList(v.tail, d+1);
    }
    say(")");
  }

  void prFieldExpList(FieldExpList f, int d) {
    indent(d);
    say("FieldExpList(");
    if (f!=null) {
      sayln("");
      say(f.name.toString()); sayln(",");
      prExp(f.init, d+1); sayln(",");
      prFieldExpList(f.tail, d+1);
    }
    say(")");
  }
}



