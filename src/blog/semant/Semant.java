/**
 *
 */
package blog.semant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import blog.absyn.Absyn;
import blog.absyn.ArrayTy;
import blog.absyn.BooleanExpr;
import blog.absyn.Dec;
import blog.absyn.DistinctSymbolDec;
import blog.absyn.DistributionDec;
import blog.absyn.DistributionExpr;
import blog.absyn.DoubleExpr;
import blog.absyn.EvidenceStmt;
import blog.absyn.ExplicitSetExpr;
import blog.absyn.Expr;
import blog.absyn.ExprList;
import blog.absyn.ExprTupleList;
import blog.absyn.FieldList;
import blog.absyn.FixedFuncDec;
import blog.absyn.FuncCallExpr;
import blog.absyn.FunctionDec;
import blog.absyn.IfExpr;
import blog.absyn.ImplicitSetExpr;
import blog.absyn.IntExpr;
import blog.absyn.ListInitExpr;
import blog.absyn.MapInitExpr;
import blog.absyn.NameTy;
import blog.absyn.NullExpr;
import blog.absyn.NumberDec;
import blog.absyn.NumberExpr;
import blog.absyn.OpExpr;
import blog.absyn.OriginFieldList;
import blog.absyn.OriginFuncDec;
import blog.absyn.ParameterDec;
import blog.absyn.QuantifiedFormulaExpr;
import blog.absyn.QueryStmt;
import blog.absyn.RandomFuncDec;
import blog.absyn.Stmt;
import blog.absyn.StmtList;
import blog.absyn.StringExpr;
import blog.absyn.SymbolArrayList;
import blog.absyn.SymbolEvidence;
import blog.absyn.SymbolExpr;
import blog.absyn.TupleSetExpr;
import blog.absyn.Ty;
import blog.absyn.TypeDec;
import blog.absyn.ValueEvidence;
import blog.common.Util;
import blog.distrib.EqualsCPD;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.ArrayType;
import blog.model.BuiltInFunctions;
import blog.model.BuiltInTypes;
import blog.model.CardinalitySpec;
import blog.model.Clause;
import blog.model.ComparisonFormula;
import blog.model.ComparisonFormula.Operator;
import blog.model.ConjFormula;
import blog.model.ConstantInterp;
import blog.model.DependencyModel;
import blog.model.DisjFormula;
import blog.model.EqualityFormula;
import blog.model.Evidence;
import blog.model.ExistentialFormula;
import blog.model.ExplicitSetSpec;
import blog.model.Formula;
import blog.model.FormulaQuery;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.FunctionSignature;
import blog.model.ImplicFormula;
import blog.model.ImplicitSetSpec;
import blog.model.ListSpec;
import blog.model.MapSpec;
import blog.model.MatrixSpec;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.NegFormula;
import blog.model.NonRandomFunction;
import blog.model.OriginFunction;
import blog.model.POP;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.model.SkolemConstant;
import blog.model.SymbolEvidenceStatement;
import blog.model.SymbolTerm;
import blog.model.Term;
import blog.model.TrueFormula;
import blog.model.TupleSetSpec;
import blog.model.Type;
import blog.model.UniversalFormula;
import blog.model.ValueEvidenceStatement;
import blog.msg.ErrorMsg;
import blog.type.Timestep;

/**
 * @author leili
 * @author amatsukawa
 * @author rbharath
 * @author awong
 * @date 2014/2/11
 */
public class Semant {

  private ErrorMsg errorMsg;
  private Model model;
  private Evidence evidence;
  private List<Query> queries;

  List<String> packages;

  // Maintains the currently active function
  private Function currFunction;

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
      String name;
      if (pkg.isEmpty()) {
        name = classname;
      } else {
        name = pkg + '.' + classname;
      }
      try {
        return Class.forName(name);
      } catch (ClassNotFoundException e) {
        // continue loop
      }
    }
    Util.fatalError("Could not load class '" + classname
        + "'; looked in the following packages: " + packages);
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
    Function f = model.getFunction(new FunctionSignature(name, argTypeList));
    if ((f == null) && (evidence != null)) {
      f = evidence.getSkolemConstant(name);
    }
    return f;
  }

  /**
   * translate the NameTy to internal BLOG type
   * 
   * @param type
   * @return
   *         internal BLOG type
   *         if not found, it will produce an error message
   */
  Type getNameType(NameTy type) {
    Type ty = null;
    String name = type.name.toString();
    ty = Type.getType(name);
    if (ty == null) {
      error(type.line, type.col, "Type " + name + " undefined!");
    }
    return ty;
  }

  // // TODO: fix list type!!!
  // Type getListType(Ty type) {
  // Type ty = null;
  // if (type instanceof ListTy) {
  // Type elementType = getNameType(((ListTy) type).typ);
  // String name = "List<" + elementType.getName() + ">";
  // System.out.println(name);
  // Type listType = model.getType(name);
  //
  // if (listType == null) {
  // error(type.line, type.col, "Type " + name + " undefined!");
  // }
  // } else {
  // error(type.line, type.col, "Type not allowed!");
  // }
  // return ty;
  // }

  ArrayType getArrayType(ArrayTy type) {
    Type termType = getType(type.typ);

    if (termType == null) {
      error(type.line, type.col, "Type " + type.typ.toString() + " undefined!");
    }

    ArrayType arrType = new ArrayType(termType);
    arrType = (ArrayType) Type.getType(arrType.getName());

    return arrType;
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
      } else if (h instanceof SymbolExpr) {
        SymbolExpr var = (SymbolExpr) h;
        String sym = var.name.toString();
        checkSymbolDup(var.line, var.col, sym);
        res.add(sym);
      } else {
        error(h.line, h.col, "Invalid expression: expecting Symbol names");
      }
    }
    return res;
  }

  Type getType(Ty type) {
    if (type instanceof NameTy) {
      return getNameType((NameTy) type);
    }
    // TODO list type
    // else if (type instanceof ListTy) {
    // return getListType(type);
    // }
    else if (type instanceof ArrayTy) {
      return getArrayType((ArrayTy) type);
    }
    if (type != null)
      error(type.line, type.col, "Type not allowed!");
    return null;
  }

  /**
   * create default library search packages for distribution function class
   */
  protected void initialize() {
    packages = new ArrayList<String>();
    packages.add("");
    packages.add("blog.distrib");
  }

  public void addPackages(List<String> pkgs) {
    packages.addAll(pkgs);
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
    Type type = getType(e.type);
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
            for (int i = 0; i < sz; i++) {
              model.addEnumeratedObject(name + "[" + i + "]", type);
            }
          }
      }
    }
  }

  /**
   * translate Function declaration to internal representation
   * for nonrandom and random functions, this step will not process the body
   * 
   * @param e
   */
  void transDec(FunctionDec e) {
    Type resTy = getType(e.result);
    if (resTy == null) {
      error(e.line, e.col, "Symbol at line " + e.result.line + " col "
          + e.result.col + " does not have a type!");
      return;
    }
    List<Type> argTy = new ArrayList<Type>();
    List<String> argVars = new ArrayList<String>();
    for (FieldList fl = e.params; fl != null; fl = fl.next) {
      Type ty = getType(fl.head.typ);
      argTy.add(ty);
      if (fl.head.var != null) {
        String vn = fl.head.var.toString();
        if (argVars.contains(vn)) {
          error(fl.line, fl.col, "Variable " + vn + " used multiple times");
        } else {
          argVars.add(vn);
        }
      }
    }

    // Handling to attach array type to list expression
    // TODO need to remove this part.
    // if (e.result instanceof ArrayTy) {
    // if (e.body instanceof ListInitExpr) {
    // ((ListInitExpr) e.body).type = e.result;
    // } else if (e.body instanceof DistributionExpr) {
    // // Nothing yet
    // } else if (e.body instanceof SymbolExpr) {
    // // Nothing yet
    // } else if (e.body instanceof IfExpr) {
    // // Nothing yet
    // } else {
    // error(e.line, e.col, "Cannot create array from non-list syntax!");
    // }
    // }

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
      NonRandomFunction f;
      if (argTy.size() == 0) {
        f = NonRandomFunction.createConstant(name, resTy, e.body);
      } else {
        f = new NonRandomFunction(name, argTy, resTy);
      }
      fun = f;
    } else if (e instanceof RandomFuncDec) {
      // dependency statement will added later
      RandomFunction f = new RandomFunction(name, argTy, resTy, null);
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

  /**
   * translate the function body
   * only nonrandom and random functions will be processed in this step.
   * 
   * @param e
   */
  void transFuncBody(FunctionDec e) {
    if (e instanceof OriginFuncDec)
      return;
    List<Type> argTy = new ArrayList<Type>();
    for (FieldList fl = e.params; fl != null; fl = fl.next) {
      Type ty = getType(fl.head.typ);
      argTy.add(ty);
    }

    String name = e.name.toString();
    Function fun = getFunction(name, argTy);
    currFunction = fun;

    if (e instanceof FixedFuncDec) {
      if (e.body == null) {
        error(e.line, e.col, "empty fixed function body");
      } else if (argTy.size() > 0) {
        if (e.body instanceof FuncCallExpr) {
          FuncCallExpr fc = (FuncCallExpr) e.body;
          List<ArgSpec> args = transExprList(fc.args, false);
          Class cls = getClassWithName(fc.func.toString());
          ((NonRandomFunction) fun).setInterpretation(cls, args);
        } else if (e.body instanceof DoubleExpr) {
          List<Object> args = new ArrayList<Object>();
          args.add(((DoubleExpr) e.body).value);
          ConstantInterp constant = new ConstantInterp(args);
          ((NonRandomFunction) fun).setInterpretation(constant);
        } else if (e.body instanceof IntExpr) {
          List<Object> args = new ArrayList<Object>();
          args.add(((IntExpr) e.body).value);
          ConstantInterp constant = new ConstantInterp(args);
          ((NonRandomFunction) fun).setInterpretation(constant);
        } else if (e.body instanceof StringExpr) {
          List<Object> args = new ArrayList<Object>();
          args.add(((StringExpr) e.body).value);
          ConstantInterp constant = new ConstantInterp(args);
          ((NonRandomFunction) fun).setInterpretation(constant);
        } else {
          // TODO: Implement more general fixed functions
        }
      } else {
        // note will do type checking later
        Object funcBody = transExpr(e.body);
        ArgSpec funcValue = (ArgSpec) funcBody;
        ((NonRandomFunction) fun).setInterpretation(
            blog.model.ConstantInterp.class,
            Collections.singletonList(funcValue));
      }
    } else if (e instanceof RandomFuncDec) {
      DependencyModel dm = transDependency(e.body, fun.getRetType(),
          fun.getDefaultValue());
      ((RandomFunction) fun).setDepModel(dm);
    }

    currFunction = null;
  }

  /**
   * check whether the actual type matches the expected type.
   * 
   * @param ty
   * @param value
   * @return
   */
  ArgSpec getTypedValue(Type ty, ArgSpec value) {
    if (value instanceof Term) {
      Type valuetype = ((Term) value).getType();
      if (valuetype.isSubtypeOf(ty))
        return value;
      else
        return null;
    } else if (value instanceof Formula) {
      if (ty == BuiltInTypes.BOOLEAN)
        return value;
      else
        return null;
    } else if (value instanceof MatrixSpec) {
      if (ty.isSubtypeOf(BuiltInTypes.REAL_MATRIX)) {
        return value;
      } else {
        return null;
      }
    } else if (value instanceof ListSpec) {
      if (ty.isSubtypeOf(BuiltInTypes.REAL_MATRIX)) {
        return ((ListSpec) value).transferToMatrix();
      } else if (ty.isSubtypeOf(BuiltInTypes.REAL_ARRAY)) {
        return ((ListSpec) value).transferToMatrix();
      } else {
        return null;
      }
    } else
      return null;
  }

  DependencyModel transDependency(Expr e, Type resTy, Object defVal) {
    Object body = transExpr(e);
    List<Clause> cl = new ArrayList<Clause>(1);
    if (body instanceof Term || body instanceof Formula
        || body instanceof TupleSetSpec) {
      cl.add(new Clause(TrueFormula.TRUE, EqualsCPD.class, Collections
          .<ArgSpec> emptyList(), Collections.singletonList((ArgSpec) body)));
    } else if (body instanceof Clause) {
      cl.add((Clause) body);
    } else if (e instanceof IfExpr) {
      cl = (List<Clause>) body;
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
    } else if (e instanceof SymbolEvidence) {
      transEvi((SymbolEvidence) e);
    } else {
      error(e.line, e.col, "Unsupported Evidence type: " + e);
    }
  }

  /**
   * valid evidence format include (will be checked in semantic checking)
   * 
   * - general form: random expression = fixed expression
   * - number_evidence: # implicit_set = int constant
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
    } else if (left instanceof ArgSpec) {
      // general value expression
      Object value = transExpr(e.right);
      if (value instanceof ArgSpec) {
        // need more semantic checking on type match
        // if (left instanceof Term) {
        // value = getTypedValue(((Term) left).getType(), (ArgSpec) value);
        // }
        // let us use the type checking in Evidence,
        if (value != null)
          evidence.addValueEvidence(new ValueEvidenceStatement((ArgSpec) left,
              (ArgSpec) value));
        else
          error(e.line, e.col,
              "type mistach for observation or translation error");
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
   * symbol_evidence format: implicit_set = explicit_set
   * 
   * @param e
   */
  void transEvi(SymbolEvidence e) {
    Object left = transExpr(e.left);
    if (left instanceof ImplicitSetSpec) {
      // symbol evidence
      // implicit set = set of ids
      ImplicitSetSpec leftset = (ImplicitSetSpec) left;
      List<String> value = null;
      if (e.right instanceof ExplicitSetExpr) {
        // ok
        value = getSymbolList(((ExplicitSetExpr) e.right).values);
      } else {
        error(
            e.right.line,
            e.right.col,
            "Invalid expression on right side of symbol evidence: explicit set of symbols expected");
      }
      SymbolEvidenceStatement sevid = new SymbolEvidenceStatement(leftset,
          value);
      if (!evidence.addSymbolEvidence(sevid)) {
        error(e.right.line, e.right.col, "Duplicate names in symbol evidence.");
      }
      for (SkolemConstant obj : sevid.getSkolemConstants()) {
        model.addFunction(obj);
      }

      // // add value evidence of this cardinality spec also!!!
      // evidence.addValueEvidence(new ValueEvidenceStatement(new
      // CardinalitySpec(
      // leftset), createSpecFromInt(value.size())));
    } else {
      error(
          e.left.line,
          e.left.col,
          "Invalid expression on left side of symbool evidence: implicit set of symbols expected");
    }
  }

  /**
   * translate number statement to model representation
   * 
   * @param e
   */
  void transDec(NumberDec e) {
    Type typ = getType(e.typ);

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
    if (Type.getType(name) != null) {
      error(e.line, e.col, "Type " + name + " already defined!");
    } else {
      model.addType(name);
      // BuiltInTypes.addArrayTypes(name);
    }
  }

  Clause transExpr(DistributionExpr e) {
    /*
     * TODO 1: Handle map expressions, not just lists
     */
    Class cls = getClassWithName(e.name.toString());
    if (cls == null) {
      error(e.line, e.col, "Class not found: " + e.name);
    }

    List<ArgSpec> as = null;
    if (e.args != null) {
      as = transExprList(e.args, true);
    }

    Clause c = new Clause(TrueFormula.TRUE, cls, as);
    c.setLocation(e.line);
    return c;
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
      return transExpr((ImplicitSetExpr) e);
    } else if (e instanceof ExplicitSetExpr) {
      return transExpr((ExplicitSetExpr) e);
    } else if (e instanceof TupleSetExpr) {
      return transExpr((TupleSetExpr) e);
    } else if (e instanceof IfExpr) {
      return transExpr((IfExpr) e);
    } else if (e instanceof OpExpr) {
      return transExpr((OpExpr) e);
    } else if (e instanceof FuncCallExpr) {
      return transExpr((FuncCallExpr) e);
    } else if (e instanceof ListInitExpr) {
      return transExpr((ListInitExpr) e);
    } else if (e instanceof MapInitExpr) {
      return transExpr((MapInitExpr) e);
    } else if (e instanceof SymbolExpr) {
      return transExpr((SymbolExpr) e);
    } else if (e instanceof NullExpr) {
      return transExpr((NullExpr) e);
    } else if (e instanceof QuantifiedFormulaExpr) {
      return transExpr((QuantifiedFormulaExpr) e);
    }
    return null;
  }

  ArgSpec transExpr(NullExpr e) {
    Term t = new FuncAppTerm(BuiltInFunctions.NULL, Collections.EMPTY_LIST);
    t.setLocation(e.line);
    return t;
  }

  ArgSpec transExpr(SymbolExpr e) {
    Term t = new SymbolTerm(e.name.toString());
    t.setLocation(e.line);
    return t;
  }

  ArgSpec transExpr(FuncCallExpr e) {
    List<ArgSpec> args = transExprList(e.args, true);
    List<Type> argTypes = new ArrayList<Type>();
    // TODO put type checking code here
    for (ArgSpec as : args) {
      // argTypes.add(as.get)
      // to add type for this argspec
    }

    Term t = new FuncAppTerm(e.func.toString(), args.toArray(new ArgSpec[args
        .size()]));
    t.setLocation(e.line);
    return t;
  }

  ArgSpec transExpr(ListInitExpr e) {
    List<ArgSpec> values = transExprList(e.values, true);
    // todo support stack and concatenation
    return new ListSpec(values, getType(e.type));
  }

  MapSpec transExpr(MapInitExpr e) {
    List<ArgSpec> probKeys = new ArrayList<ArgSpec>();
    List<Object> probs = new ArrayList<Object>();
    ExprTupleList mapExprs = e.values;
    while (mapExprs != null) {
      probKeys.add((ArgSpec) transExpr(mapExprs.from));
      probs.add(transExpr(mapExprs.to));
      mapExprs = mapExprs.next;
    }
    MapSpec m = new MapSpec(probKeys, probs);
    return m;
  }

  Formula transExpr(QuantifiedFormulaExpr e) {
    Object quantExpr = transExpr(e.formula);
    if (!(quantExpr instanceof Formula)) {
      return null;
    }
    Formula quantFormula = (Formula) quantExpr;
    Type quantType = getType(e.type);
    if (e.quantifier == QuantifiedFormulaExpr.FORALL) {
      return new UniversalFormula(e.var.toString(), quantType, quantFormula);
    } else if (e.quantifier == QuantifiedFormulaExpr.EXISTS) {
      return new ExistentialFormula(e.var.toString(), quantType, quantFormula);
    } else {
      return null;
    }
  }

  /**
   * combine clauses from if
   * 
   * @param test
   * @param value
   * @param clauses
   */
  void combineFormula(Formula test, Object value, List<Clause> clauses) {
    if (value instanceof Clause) {
      Clause c = (Clause) value;
      clauses.add(addTestConditionToClause(test, c));
    } else if (value instanceof List<?>) {
      for (Object v : ((List<?>) value)) {
        Clause c = (Clause) v;
        clauses.add(addTestConditionToClause(test, c));
      }
    } else {
      // should be ArgSpec
      clauses.add(new Clause(test, EqualsCPD.class, Collections
          .<ArgSpec> emptyList(), Collections.singletonList((ArgSpec) value)));
    }
  }

  private Clause addTestConditionToClause(Formula test, Clause c) {
    Formula old = c.getCond();
    Formula ne = createConjunction(test, old);
    c.setCond(ne);
    return c;
  }

  private Formula createConjunction(Formula c1, Formula c2) {
    if (c2 == TrueFormula.TRUE)
      return c1;
    if (c1 == TrueFormula.TRUE)
      return c2;
    return new ConjFormula(c1, c2);
  }

  /**
   * combine clauses from else
   * 
   * @param value
   * @param clauses
   */

  void combineFormula(Object value, List<Clause> clauses) {
    if (value instanceof Clause) {
      clauses.add((Clause) value);
    } else if (value instanceof List<?>) {
      clauses.addAll((List<Clause>) value);
    } else {
      // should be ArgSpec
      clauses.add(new Clause(TrueFormula.TRUE, EqualsCPD.class, Collections
          .<ArgSpec> emptyList(), Collections.singletonList((ArgSpec) value)));
    }
  }

  List<Clause> transExpr(IfExpr e) {
    ArrayList<Clause> clauses = new ArrayList<Clause>();
    Formula test = TrueFormula.TRUE;

    // TODO: write a test for the SymbolTerm case to exclude non-Boolean
    // variables/functions
    Object cond = transExpr(e.test);
    if (cond instanceof Formula) {
      test = (Formula) cond;
    } else if (cond instanceof Term) {
      test = new EqualityFormula((Term) cond,
          BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
    } else {
      error(e.test.line, e.test.col,
          "Cannot use non-Boolean value as predicate for if clause");
      System.exit(1);
    }

    Object thenClause = transExpr(e.thenclause);
    combineFormula(test, thenClause, clauses);
    if (e.elseclause != null) {
      Object elseClause = transExpr(e.elseclause);
      combineFormula(elseClause, clauses);
    }
    return clauses;
  }

  ArgSpec transExpr(BooleanExpr e) {
    Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
        String.valueOf(e.value), BuiltInTypes.BOOLEAN, e.value));
    t.setLocation(e.line);
    return t;
  }

  ArgSpec transExpr(IntExpr e) {
    Term t = new FuncAppTerm(BuiltInFunctions.getLiteral(
        String.valueOf(e.value), BuiltInTypes.INTEGER, e.value));
    t.setLocation(e.line);
    return t;
  }

  ArgSpec transExpr(StringExpr e) {
    Term t = new FuncAppTerm(BuiltInFunctions.getLiteral("\"" + e.value + "\"",
        BuiltInTypes.STRING, e.value));
    t.setLocation(e.line);
    return t;
  }

  ExplicitSetSpec transExpr(ExplicitSetExpr e) {
    List terms = new ArrayList();

    ExprList currTerm = e.values;
    while (currTerm != null) {
      terms.add(transExpr(currTerm.head));
      currTerm = currTerm.next;
    }
    return new ExplicitSetSpec(terms);
  }

  ImplicitSetSpec transExpr(ImplicitSetExpr e) {
    Type typ = getType(e.typ);
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

  TupleSetSpec transExpr(TupleSetExpr e) {
    List<Term> tupleTerms = new ArrayList<Term>();
    List<Type> varTypes = new ArrayList<Type>();
    List<String> varNames = new ArrayList<String>();
    Formula cond = TrueFormula.TRUE;

    while (e.setTuple != null) {
      Object tuple = transExpr(e.setTuple.head);
      if (tuple instanceof Term) {
        tupleTerms.add((Term) tuple);
      } else {
        error(
            e.cond.line,
            e.cond.col,
            "Invalid expression as term in tuple set: term (number, string, boolean, or function call) expected");
      }
      e.setTuple = e.setTuple.next;
    }

    // TODO: TRANSLATE THE VARIABLE LIST AND TYPES
    while (e.enumVars != null) {
      Object varType = this.getType(e.enumVars.head.typ);
      Object varName = e.enumVars.head.var.toString();
      if (varType instanceof Type && varName instanceof String) {
        varTypes.add((Type) varType);
        varNames.add((String) varName);
      } else {
        error(
            e.cond.line,
            e.cond.col,
            "Invalid expression as logical variable in implicit set: logical variable expected");
      }
      e.enumVars = e.enumVars.next;
    }

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
    return new TupleSetSpec(tupleTerms, varTypes, varNames, cond);
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

  Object transExpr(OpExpr e) {
    Object left = null, right = null;
    Term term;
    if (e.left != null) {
      left = transExpr(e.left);
    }
    if (e.right != null) {
      right = transExpr(e.right);
    }
    String funcname = null;

    switch (e.oper) {
    case OpExpr.PLUS:
      funcname = BuiltInFunctions.PLUS_NAME;
      break;
    case OpExpr.MINUS:
      funcname = BuiltInFunctions.MINUS_NAME;
      break;
    case OpExpr.MULT:
      funcname = BuiltInFunctions.MULT_NAME;
      break;
    case OpExpr.DIV:
      funcname = BuiltInFunctions.DIV_NAME;
      break;
    case OpExpr.MOD:
      funcname = BuiltInFunctions.MOD_NAME;
      break;
    case OpExpr.POWER:
      funcname = BuiltInFunctions.POWER_NAME;
      break;
    case OpExpr.EQ:
      return new EqualityFormula((Term) left, (Term) right);
    case OpExpr.NEQ:
      return new NegFormula(new EqualityFormula((Term) left, (Term) right));
    case OpExpr.LT:
      return new ComparisonFormula((Term) left, (Term) right, Operator.LT);
    case OpExpr.LEQ:
      return new ComparisonFormula((Term) left, (Term) right, Operator.LEQ);
    case OpExpr.GT:
      return new ComparisonFormula((Term) left, (Term) right, Operator.GT);
    case OpExpr.GEQ:
      return new ComparisonFormula((Term) left, (Term) right, Operator.GEQ);
    case OpExpr.AND:
      if (left instanceof Term) {
        left = new EqualityFormula((Term) left,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      if (right instanceof Term) {
        right = new EqualityFormula((Term) right,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      return new ConjFormula((Formula) left, (Formula) right);
    case OpExpr.OR:
      if (left instanceof Term) {
        left = new EqualityFormula((Term) left,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      if (right instanceof Term) {
        right = new EqualityFormula((Term) right,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      return new DisjFormula((Formula) left, (Formula) right);
    case OpExpr.IMPLY:
      if (left instanceof Term) {
        left = new EqualityFormula((Term) left,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      if (right instanceof Term) {
        right = new EqualityFormula((Term) right,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      return new ImplicFormula((Formula) left, (Formula) right);
    case OpExpr.NOT:
      if (right instanceof Term) {
        right = new EqualityFormula((Term) right,
            BuiltInTypes.BOOLEAN.getCanonicalTerm(true));
      }
      return new NegFormula((Formula) right);
    case OpExpr.SUB:
      Function func;
      if (left instanceof SymbolTerm) {
        if (e.right instanceof IntExpr) {
          String objectname = ((SymbolTerm) left).getName() + '['
              + ((IntExpr) e.right).value + ']';
          Function object = getFunction(objectname, Collections.EMPTY_LIST);
          if (object != null)
            return new FuncAppTerm(object);
        }
        Object symbolMapping = model
            .getFuncsWithName(((SymbolTerm) left).getName()).iterator().next();
        if (((Function) symbolMapping).getRetType() == BuiltInTypes.REAL_ARRAY) {
          func = BuiltInFunctions.SUB_REAL_ARRAY;
        } else if (((Function) symbolMapping).getRetType() == BuiltInTypes.INTEGER_ARRAY) {
          func = BuiltInFunctions.SUB_INT_ARRAY;
        } else {
          func = BuiltInFunctions.SUB_MAT;
        }
      } else {
        // a hack now, need to consider IntegerMatrix as well.
        func = BuiltInFunctions.SUB_MAT;
      }
      term = new FuncAppTerm(func, (ArgSpec) left, (ArgSpec) right);
      return term;
    case OpExpr.AT:
      if (e.left == null && e.right instanceof IntExpr) {
        Timestep t = Timestep.at(((IntExpr) e.right).value);
        term = new FuncAppTerm(BuiltInFunctions.getLiteral(t.toString(),
            BuiltInTypes.TIMESTEP, t));
        term.setLocation(e.line);
        return term;
      }

    default:
      error(e.getLine(), e.getCol(),
          "The operation could not be applied" + e.toString());
      return null;
    }

    // TODO remove the following lines after testing. (leili)
    // if (e.left != null)
    // sig = new FunctionSignature(funcname, leftType, rightType);
    // else
    // sig = new FunctionSignature(funcname, rightType);
    // NonRandomFunction func = BuiltInFunctions.getFunction(sig);
    // if (func != null) {
    if (e.left != null)
      // term = new FuncAppTerm(func, (Term) left, (Term) right);
      term = new FuncAppTerm(funcname, (Term) left, (Term) right);
    else
      term = new FuncAppTerm(funcname, (Term) right);
    return term;
    // } else {
    // Util.fatalError("No operator " + funcname + " for operands of type "
    // + leftType + "," + rightType + "!");
    // throw new IllegalArgumentException("Cannot perform operation!");
    // }
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
      if (o != null) {
        if (o instanceof ArgSpec) {
          args.add((ArgSpec) o);
        } else {
          error(e.line, e.col,
              "Expression expected! but we get " + o.toString());
        }
      }
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

  void transStmt(Stmt e) {
    if (e instanceof Dec) {
      transDec((Dec) e);
    } else if (e instanceof EvidenceStmt) {
      transEvi((EvidenceStmt) e);
    } else if (e instanceof QueryStmt) {
      transQuery((QueryStmt) e);
    }
  }

  /**
   * each statement list will be processed twice
   * first time everything except processing function body for random/nonrandom
   * functions
   * second time those function bodies
   * 
   * @param stl
   */
  void transStmtList(StmtList stl) {

  }

  /**
   * semantic checking and translate the BLOG program to model representation
   * 
   * @param e
   * @return whether any error happened during parsing and translating
   */
  public boolean transProg(Absyn e) {
    if (e instanceof StmtList) {
      StmtList stl = (StmtList) e;
      List<Stmt> stmts = new LinkedList<Stmt>();
      List<FunctionDec> funs = new LinkedList<FunctionDec>();
      for (; stl != null; stl = stl.next) {
        if (stl.head instanceof Dec) {
          transStmt(stl.head);
          if (stl.head instanceof FunctionDec)
            funs.add((FunctionDec) stl.head);
        } else {
          stmts.add(stl.head);
        }
      }

      // second pass translate function body
      for (FunctionDec fd : funs)
        transFuncBody(fd);

      // type checking
      if (!model.checkTypesAndScope()) {
        error(0, 0, "type checking failed");
      }

      // third pass: translate observation and statement
      for (Stmt stm : stmts) {
        transStmt(stm);
      }

      if (!evidence.checkTypesAndScope(model)) {
        error(0, 0, "type checking failed for evidence");
      }

      for (Iterator iter = queries.iterator(); iter.hasNext();) {
        Query q = (Query) iter.next();
        if (!q.checkTypesAndScope(model)) {
          error(0, 0, "type checking failed for query");
        }
      }

    } else {
      error(0, 0, "Invalid program");
    }
    return errorMsg.OK();
  }

  public ModelEvidenceQueries getModelEvidenceQueries() {
    return new ModelEvidenceQueries(model, evidence, queries);
  }

}
