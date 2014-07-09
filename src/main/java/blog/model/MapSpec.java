package blog.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.common.UnaryProcedure;
import blog.distrib.EqualsCPD;
import blog.sample.EvalContext;

/**
 * ArgSpec that represents a map statement, such as
 * {Blue -> 0.5, Green -> 0.5}.
 * For now, only supports fixed(nonrandom) objects.
 * 
 * @author amatsukawa
 * @author leili (2012/07/20 modified)
 */

public class MapSpec extends ArgSpec {

  Map<ArgSpec, Object> map;
  boolean compiled;

  // Until compilation, need these lists to store values
  List<ArgSpec> keys;
  List<Object> values;

  // if this flag is true, the values will not be evaluated
  boolean lazyEval = false;

  public void setLazyEval(boolean flag) {
    lazyEval = flag;
  }

  /**
   * Create empty ArgSpec,
   * Add Elements to it later on.
   */
  public MapSpec() {
    this.map = new HashMap<ArgSpec, Object>();
    compiled = false;
  }

  public MapSpec(Map<ArgSpec, Object> m) {
    // TODO need to check the type consistency
    this.map = m;
    compiled = false;
  }

  public MapSpec(List<ArgSpec> objs, List<Object> probs) {
    this.map = new HashMap<ArgSpec, Object>();
    compiled = false;
    keys = objs;
    values = probs;
  }

  /**
   * TODO: Need to check correctness
   * 
   * To compile a map, compile each of its possible values,
   * then hash K/V pairs of objects and probs into map
   */
  public int compile(LinkedHashSet callStack) {
    compiled = true;
    callStack.add(this);
    int errors = 0;
    if (keys.isEmpty()) {
      System.err.println("Map expression is empty");
      errors = 1;
    } else {
      for (ArgSpec arg : keys) {
        errors += arg.compile(callStack);
      }
      for (Object o : values) {
        if (o instanceof Term) {
          Term t = (Term) o;
          errors += t.compile(callStack);
        } else if (o instanceof DistribSpec) {
          DistribSpec d = (DistribSpec) o;
          errors += d.compile(callStack);
        } else if (o instanceof CaseSpec) {
          CaseSpec c = (CaseSpec) o;
          errors += c.compile(callStack);
        } else {
          throw new IllegalArgumentException(
              "Map values must "
                  + "either be terms or clauses (if-then-else, case, distributions)!");
        }
      }

      for (int i = 0; i < keys.size(); i++) {
        map.put(keys.get(i), values.get(i));
      }
    }

    callStack.remove(this);
    return errors;
  }

  /**
	 * 
	 */
  public Map<Object, Object> evaluate(EvalContext context) {
    HashMap<Object, Object> newmap = new HashMap<Object, Object>();
    for (Map.Entry<ArgSpec, Object> entry : map.entrySet()) {
      Object k = entry.getKey().evaluate(context);
      Object v = entry.getValue();
      if (!lazyEval) { // not lazy evaluate, we need evaluate all that we can do
        if (v instanceof ArgSpec)
          v = ((ArgSpec) v).evaluate(context); // We only evaluate fixed value
      }
      newmap.put(k, v);
    }
    return newmap;
  }

  public Map<ArgSpec, Object> getMap() {
    return map;
  }

  /**
   * 
   */
  public boolean containsRandomSymbol() {
    if (compiled) {
      for (Map.Entry<ArgSpec, Object> entry : map.entrySet()) {
        if (entry.getKey().containsRandomSymbol())
          return true;
        Object obj = entry.getValue();
        if (obj instanceof ArgSpec) {
          if (((ArgSpec) obj).containsRandomSymbol()) {
            return true;
          }
          // TODO check whether it is a number
        }
      }
      return false;
    }
    for (ArgSpec as : keys) {
      if (as.containsRandomSymbol()) {
        return true;
      }
    }
    for (Object obj : values) {
      if (obj instanceof ArgSpec) {
        if (((ArgSpec) obj).containsRandomSymbol()) {
          return true;
        }
        // TODO check whether it is a number
      }
    }
    return false;
  }

  /*
   * This will be called in CaseSpec
   * We call this method to ensure that all the value of this map
   * will be a distribSpec
   * No Term is allowed here!
   * 
   * @return: recompile and return the number of errors
   */
  public int enforceDistribSpec() {
    int errors = 0;
    for (int i = 0; i < keys.size(); ++i) {
      if ((values.get(i) instanceof Term) || (values.get(i) instanceof Formula)
          || (values.get(i) instanceof TupleSetSpec)) {
        ArgSpec t = (ArgSpec) values.get(i);
        DistribSpec dis = new DistribSpec(EqualsCPD.class, t);
        errors += dis.initCPD();
        values.set(i, dis);
        map.put(keys.get(i), dis);
      }
    }
    return errors;
  }

  public boolean checkTypesAndScope(Model model, Map scope) {
    return checkTypesAndScope(model, scope, null);
  }

  public boolean checkTypesAndScope(Model model, Map scope, Type childType) {
    // Check typing of all symbols in the map
    for (ArgSpec obj : keys) {
      if (!obj.checkTypesAndScope(model, scope)) {
        return false;
      }
    }

    for (Object val : values) {
      if (val instanceof ArgSpec) {
        ArgSpec as = (ArgSpec) val;
        if (!as.checkTypesAndScope(model, scope)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns term in ArgSpec equal to a given term t, or null if there isn't
   * any.
   */
  public ArgSpec find(Term t) {
    if (this.equals(t))
      return this;
    ArgSpec ret = null;
    for (ArgSpec key : keys) {
      ret = key.find(t);
      if (ret != null)
        return ret;
    }
    for (Object val : values) {
      if (val instanceof ArgSpec) {
        ret = ((ArgSpec) val).find(t);
        if (ret != null)
          return ret;
      }
    }
    return null;
  }

  /**
   * Applies a procedure to all terms in this ArgSpec which satisfy a given
   * predicate to a given collection.
   */
  public void applyToTerms(UnaryProcedure procedure) {
    for (ArgSpec key : keys)
      key.applyToTerms(procedure);
    for (Object val : values) {
      if (val instanceof ArgSpec) {
        ((ArgSpec) val).applyToTerms(procedure);
      }
    }
  }

  /**
   * Returns an ArgSpec resulting from the replacement of all occurrences of a
   * term by another, if there is any, or self. A new ArgSpec is compiled if
   * this is compiled.
   */
  public ArgSpec replace(Term t, ArgSpec another) {
    List<ArgSpec> newKeys = new LinkedList<ArgSpec>();
    for (ArgSpec key : keys) {
      newKeys.add(key.replace(t, another));
    }
    List<Object> newValues = new LinkedList<Object>();
    for (Object val : values) {
      if (val instanceof ArgSpec) {
        newValues.add(((ArgSpec) val).replace(t, another));
      } else
        newValues.add(val);
    }
    if (newKeys.equals(keys) && newValues.equals(values))
      return this;
    MapSpec mp = new MapSpec(newKeys, newValues);
    if (compiled)
      mp.compile(new LinkedHashSet());
    return mp;
  }

  /**
   * Returns the result of applying the substitution <code>subst</code> to this
   * expression, excluding the logical variables in <code>boundVars</code>. This
   * method is used for recursive calls. The set <code>boundVars</code> should
   * contain those variables that are bound in the syntax tree between this
   * sub-expression and the top-level expression to which the substitution is
   * being applied.
   */
  public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
    List<ArgSpec> newKeys = new LinkedList<ArgSpec>();
    for (ArgSpec key : keys) {
      newKeys.add(key.getSubstResult(subst, boundVars));
    }
    List<Object> newValues = new LinkedList<Object>();
    for (Object val : values) {
      if (val instanceof ArgSpec) {
        newValues.add(((ArgSpec) val).getSubstResult(subst, boundVars));
      } else
        newValues.add(val);
    }
    return new MapSpec(newKeys, newValues);
  }

  public String toString() {
    String contents = "{";
    for (int i = 0; i < keys.size(); i++) {
      contents += keys.get(i) + " -> " + values.get(i);
      if (i < keys.size() - 1) {
        contents += ", ";
      }
    }
    return contents + "}";
  }
}
