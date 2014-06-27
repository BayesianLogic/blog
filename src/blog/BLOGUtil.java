package blog;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Queries;
import blog.sample.AfterSamplingListener;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.world.PartialWorld;

/**
 * A class defining static helper methods on basic interfaces (therefore not
 * belonging to any of their specific implementations).
 * 
 * @author Rodrigo
 */
public class BLOGUtil {

  /**
   * Ensures given variables are instantiated and supported in a given context,
   * sampling further variables if necessary.
   */
  public static void ensureDetAndSupported(
      Collection<? extends BayesNetVar> vars, InstantiatingEvalContext context) {
    for (BayesNetVar var : vars) {
      var.ensureDetAndSupported(context);
    }
  }

  /**
   * Ensures given variables are instantiated and supported in a given world,
   * sampling further variables if necessary.
   */
  public static void ensureDetAndSupported(
      Collection<? extends BayesNetVar> vars, PartialWorld world) {
    ensureDetAndSupported(vars, new ClassicInstantiatingEvalContext(world));
  }

  /**
   * Shorthand for {@link #ensureDetAndSupported(Collection, PartialWorld)}
   * applied to a single variable.
   */
  public static void ensureDetAndSupported(BayesNetVar var, PartialWorld world) {
    ensureDetAndSupported(Util.list(var), world);
  }

  /**
   * Same as {@link #ensureDetAndSupported(BayesNetVar, PartialWorld)}, which a
   * variable sampling listener.
   */
  public static void ensureDetAndSupportedWithListener(Collection vars,
      PartialWorld world, AfterSamplingListener afterSamplingListener) {
    ClassicInstantiatingEvalContext context1 = new ClassicInstantiatingEvalContext(
        world);
    context1.afterSamplingListener = afterSamplingListener;
    ClassicInstantiatingEvalContext context = context1;
    ensureDetAndSupported(vars, context);
  }

  /**
   * Sets the value of a variable in a world, if this variable is basic (you
   * cannot set the value of a derived variable).
   */
  public static void setIfBasicVar(BayesNetVar var, Object value,
      PartialWorld world) {
    if (var instanceof BasicVar)
      world.setValue((BasicVar) var, value);
  }

  /**
   * Sets given evidence in a given world (only basic variables -- derived vars
   * will be a consequence of basic variables anyway).
   */
  public static void setBasicVars(Evidence evidence, PartialWorld world) {
    for (BayesNetVar var : evidence.getEvidenceVars()) {
      if (var instanceof BasicVar)
        world.setValue((BasicVar) var, evidence.getObservedValue(var));
    }
  }

  public static double setAndGetProbability(BayesNetVar rv, Object value,
      PartialWorld world) {
    BLOGUtil.ensureDetAndSupported(rv, world);
    BLOGUtil.setIfBasicVar(rv, value, world);
    return world.getProbOfValue(rv);
  }

  /**
   * Returns an ArgSpec parsed from a string description, according to a model.
   */
  public static ArgSpec parseArgSpec(String description, Model model) {
    Queries queries = new Queries(model);
    queries.addFromString("query " + description + ";");
    if (queries.size() != 1) {
      throw new IllegalArgumentException("Parsed " + queries.size()
          + " queries instead of a single one");
    }
    return ((ArgSpecQuery) queries.get(0)).argSpec();
  }

  /**
   * Returns a collection with the BayesNetVars associated to a set of queries.
   */
  public static Collection getQueriesVars(Collection queries) {
    Collection result = new LinkedList();
    for (Iterator iter = queries.iterator(); iter.hasNext();) {
      BayesNetVar var = ((ArgSpecQuery) iter.next()).getVariable();
      result.add(var);
    }
    return result;
  }
}
