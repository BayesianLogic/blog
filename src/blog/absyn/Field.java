package blog.absyn;

import blog.symbol.Symbol;

public class Field extends Absyn {
  public Symbol var;
  public Ty typ;

  public Field(int line, int col, Symbol var, Ty typ) {
    super(line, col);
    this.var = var;
    this.typ = typ;
  }

  public Field(int p, Symbol var, Ty typ) {
    this(0, p, var, typ);
  }

  public Field(Symbol var, Ty typ) {
    this(typ.line, typ.col, var, typ);
  }

}