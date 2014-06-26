/**
 * 
 */
package blog.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.RandFuncAppVar;
import blog.common.UnaryProcedure;
import blog.distrib.CondProbDistrib;
import blog.distrib.EqualsCPD;
import blog.sample.EvalContext;

/**
 * @author Yi Wu
 * @since 2014/6/26/
 * 
 */
public class DistribSpec extends ArgSpec {

  private String cpdName;
  private ArgSpec[] args;
  private CondProbDistrib cpd;
  private Class<? extends CondProbDistrib> cpdClass;

  public String getName() {
    return cpdName;
  }

  public CondProbDistrib getCPD() {
    return cpd;
  }

  public DistribSpec(Class<? extends CondProbDistrib> cpdClass, ArgSpec arg0) {
    cpdName = cpdClass.getName();
    this.cpdClass = cpdClass;
    args = new ArgSpec[1];
    args[0] = arg0;
  }

  public DistribSpec(Class<? extends CondProbDistrib> cpdClass,
      List<ArgSpec> argList) {
    cpdName = cpdClass.getName();
    this.cpdClass = cpdClass;
    this.args = new ArgSpec[argList.size()];
    argList.toArray(this.args);
  }

  public DistribSpec(Class<? extends CondProbDistrib> cpdClass,
      ArgSpec[] argList) {
    cpdName = cpd.getClass().getName();
    this.cpdClass = cpdClass;
    this.args = new ArgSpec[argList.length];
    for (int i = 0; i < argList.length; ++i)
      this.args[i] = argList[i];
  }

  /*
   * (non-Javadoc)
   * 
   * @return: this function will return DependencyModel.Distrib
   * 
   * @see blog.model.ArgSpec#evaluate(blog.sample.EvalContext)
   */
  @Override
  public Object evaluate(EvalContext context) {
    return this;
  }

  public DependencyModel.Distrib getDistrib(EvalContext context) {
    context.pushEvaluee(this);
    List<Object> argValues = new ArrayList<Object>();

    for (ArgSpec spec : args) {
      if (spec.containsRandomSymbol()) {
        // This is a random argument that needs to be evaluated.
        Object argValue = spec.evaluate(context);
        if (argValue == null) {
          break; // CPD arg not determined
        }
        argValues.add(argValue);
      } else {
        // This is a fixed argument that was given at CPD construction time.
        argValues.add(null);
      }
    }

    context.popEvaluee();
    if (argValues.size() == args.length) {
      // all CPD args were determined
      return new DependencyModel.Distrib(cpd, argValues);
    }
    return null;
  }

  public BasicVar getEqualParent(EvalContext context) {
    if (cpd instanceof EqualsCPD) {
      ArgSpec arg = args[0];
      if (arg instanceof FuncAppTerm) {
        FuncAppTerm t = (FuncAppTerm) arg;
        if (t.getFunction() instanceof RandomFunction) {
          Object[] argValues = new Object[t.getArgs().length];
          for (int i = 0; i < t.getArgs().length; ++i) {
            argValues[i] = t.getArgs()[i].evaluate(context);
            if (argValues[i] == null) {
              return null;
            }
          }
          return new RandFuncAppVar((RandomFunction) t.getFunction(), argValues);
        }
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#containsRandomSymbol()
   */
  @Override
  public boolean containsRandomSymbol() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#checkTypesAndScope(blog.model.Model, java.util.Map)
   */
  @Override
  public boolean checkTypesAndScope(Model model, Map scope) {
    return checkTypesAndScope(model, scope, null);
  }

  public boolean checkTypesAndScope(Model model, Map scope, Type childType) {
    boolean correct = true;
    for (ArgSpec spec : args) {
      if (!spec.checkTypesAndScope(model, scope)) {
        correct = false;
      }
    }

    // for EqualsCPD, we can do additional checking
    if (correct && (cpdClass == EqualsCPD.class)) {
      if (args.length != 1) {
        System.err.println(getLocation()
            + "EqualsCPD takes exactly one argument");
        correct = false;
      } else {
        ArgSpec arg = args[0];
        Type argType = null;
        if (arg instanceof Term) {
          argType = ((Term) arg).getType();
        } else if (arg instanceof Formula) {
          argType = BuiltInTypes.BOOLEAN;
        } else if (arg instanceof CardinalitySpec) {
          argType = BuiltInTypes.NATURAL_NUM;
        } else if (arg instanceof TupleSetSpec) {
          argType = BuiltInTypes.SET;
        } else {
          System.err.println(arg.getLocation() + ": Illegal value for "
              + "deterministic distribution: " + arg);
          correct = false;
        }

        if ((argType != null) && !argType.isSubtypeOf(childType)) {
          System.err.println(arg.getLocation()
              + ": Value for deterministic distribution has "
              + "wrong type (expected " + childType + ", got " + argType + ")");
          correct = false;
        }
      }
    }

    return correct;
  }

  public int compile(LinkedHashSet callStack) {
    callStack.add(this);
    int errors = 0;

    for (ArgSpec spec : args) {
      errors += spec.compile(callStack);
    }

    if (cpd == null) {
      errors += initCPD(callStack);
    }

    callStack.remove(this);
    return errors;
  }

  public int initCPD() {
    int errors = 0;
    try {
      Constructor<? extends CondProbDistrib> ct = cpdClass.getConstructor();
      cpd = (CondProbDistrib) ct.newInstance();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + e.getCause().getClass().getName() + " ("
          + e.getCause().getMessage() + ")");
      ++errors;
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + cpdClass + " does not have a constructor with no arguments");
      ++errors;
    } catch (ClassCastException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + cpdClass + " does not implement "
          + "the CondProbDistrib interface.");
      ++errors;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation()
          + ": couldn't instantiate class " + cpdClass);
      ++errors;
    }

    return errors;
  }

  private int initCPD(LinkedHashSet callStack) {
    int errors = 0;

    List<Object> constructionArgValues = new ArrayList<Object>();
    for (ArgSpec spec : args) {
      if (spec.containsRandomSymbol()) {
        // This is a random argument that is not passed at construction time.
        constructionArgValues.add(null);
      } else {
        // This is a fixed argument that is passed at construction time.
        int thisParamErrors = spec.compile(callStack);
        errors += thisParamErrors;
        if (thisParamErrors == 0) {
          Object val = spec.getValueIfNonRandom();
          constructionArgValues.add(val);
          if (val == null) {
            throw new IllegalStateException("non-fixed arg in cpdArgsFixed");
          }
        }
      }
    }

    if (errors > 0) {
      return errors; // can't compute parameters, so can't create CPD
    }

    try {
      Constructor<? extends CondProbDistrib> ct = cpdClass.getConstructor();
      cpd = (CondProbDistrib) ct.newInstance();
      cpd.setParams(constructionArgValues.toArray());
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + e.getCause().getClass().getName() + " ("
          + e.getCause().getMessage() + ")");
      ++errors;
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + cpdClass + " does not have a constructor with no arguments");
      ++errors;
    } catch (ClassCastException e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation() + ": "
          + cpdClass + " does not implement "
          + "the CondProbDistrib interface.");
      ++errors;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Error initializing CPD at " + getLocation()
          + ": couldn't instantiate class " + cpdClass);
      ++errors;
    }

    return errors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#find(blog.model.Term)
   */
  @Override
  public ArgSpec find(Term t) {
    for (ArgSpec arg : args) {
      ArgSpec ret = arg.find(t);
      if (ret != null)
        return ret;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#applyToTerms(blog.common.UnaryProcedure)
   */
  @Override
  public void applyToTerms(UnaryProcedure procedure) {
    for (ArgSpec arg : args) {
      arg.applyToTerms(procedure);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#replace(blog.model.Term, blog.model.ArgSpec)
   */
  @Override
  public ArgSpec replace(Term t, ArgSpec another) {
    List<ArgSpec> newArgs = new LinkedList<ArgSpec>();
    boolean differ = false;
    for (ArgSpec arg : args) {
      ArgSpec tmp = arg.replace(t, another);
      if (!tmp.equals(arg))
        differ = true;
      newArgs.add(tmp);
    }
    if (differ)
      return new DistribSpec(cpdClass, newArgs);
    else
      return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#getSubstResult(blog.model.Substitution,
   * java.util.Set)
   */
  @Override
  public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
    List<ArgSpec> newArgs = new LinkedList<ArgSpec>();
    for (ArgSpec arg : args)
      newArgs.add(arg.getSubstResult(subst, boundVars));
    return new DistribSpec(cpdClass, newArgs);
  }

  public String toString() {
    String contents = cpdName + "(";
    for (int i = 0; i < args.length; i++) {
      contents += args[i];
      if (i < args.length - 1) {
        contents += ", ";
      }
    }
    return contents + ")";
  }

}
