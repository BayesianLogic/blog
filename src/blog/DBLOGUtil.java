package blog;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.FilteredIterator;
import blog.common.UnaryPredicate;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.ConstantInterp;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.NonRandomFunction;
import blog.model.Query;
import blog.model.SymbolEvidenceStatement;
import blog.model.ValueEvidenceStatement;
import blog.type.Timestep;
import blog.world.PartialWorld;

/**
 * A class defining static helper methods on basic interfaces (therefore not
 * belonging to any of their specific implementations), regarding temporal
 * aspects (DBLOG) (general ones are in {@link BLOGUtil}.
 * 
 * @author Rodrigo
 */
public class DBLOGUtil {
  /**
   * Returns a string obtained by replacing all identifiers <code>t</code> in a
   * given string by the string representation of a timestep the index of which
   * is given.
   */
  public static String replaceTByTimeStep(String string, int timestepIndex) {
    return string.replaceAll("\\bt\\b", "@" + timestepIndex);
  }

  /**
   * Returns the query obtained by instantiating a query string with a given
   * time step.
   */
  public static ArgSpecQuery getQueryForTimestep(String queryString,
      Model model, int timestepIndex) {
    String queryForLastestTimestepString = DBLOGUtil.replaceTByTimeStep(
        queryString, timestepIndex);
    ArgSpecQuery query = BLOGUtil.parseQuery_NE("query "
        + queryForLastestTimestepString + ";", model);
    return query;
  }

  /**
   * An iterator over the time step indices present in a partial world.
   */
  public static class TimestepIndicesIterator extends FilteredIterator {
    public TimestepIndicesIterator(PartialWorld world) {
      super(world.getInstantiatedVars().iterator());
    }

    public Object filter(int index, Object varObj) {
      BayesNetVar var = (BayesNetVar) varObj;
      int timestepIndex = var.maxTimestep().getValue();
      if (timestepIndex == -1 || alreadyReturned.contains(timestepIndex)) {
        return null;
      }
      alreadyReturned.add(timestepIndex);
      return timestepIndex;
    }

    private HashSet alreadyReturned = new HashSet();
  }

  /**
   * Returns an iterator over the time step indices present in a partial world.
   */
  public static Iterator getTimestepIndicesIterator(PartialWorld world) {
    return new TimestepIndicesIterator(world);
  }

  /**
   * remove the temporal variables from the possible world that are
   * different from the specified timestep
   * 
   * @param largest
   * @param world
   */
  public static void removeVarsAtDiffTimestep(Timestep largest,
      PartialWorld world) {
    LinkedList instantiatedVars = new LinkedList(world.getInstantiatedVars());
    Iterator varIt = instantiatedVars.iterator();
    while (varIt.hasNext()) {
      BasicVar var = (BasicVar) varIt.next();
      Timestep timestep = var.maxTimestep();
      if (timestep != null && timestep != largest) {
        world.setValue(var, null);
      }
    }
  }

  public static boolean isTimestep(ArgSpec t) {
    return getTimestepInTimestepTerm(t) != null;
  }

  /**
   * Returns the single timestep in the given argspec, or <code>null</code> if
   * there is none in it, or exits if there are more than one.
   */
  public static Timestep getSingleTimestepIn(ArgSpec argSpec) {
    Collection timesteps = getTimestepTermsIn(argSpec, new LinkedList());
    if (timesteps.size() > 1)
      Util.fatalError("DBLOGUtil.getTimestep called for argspec with more than one timestep: "
          + argSpec);
    if (timesteps.size() == 0)
      return null;
    return getTimestepInTimestepTerm((ArgSpec) Util.getFirst(timesteps));

  }

  /**
   * Returns a Timestep object if this is a constant timestep term, or null
   * otherwise.
   */
  public static Timestep getTimestepInTimestepTerm(ArgSpec timestepTerm) {
    if (!(timestepTerm instanceof FuncAppTerm))
      return null;
    FuncAppTerm funcAppTerm = (FuncAppTerm) timestepTerm;
    if (!(funcAppTerm.getFunction() instanceof NonRandomFunction))
      return null;
    NonRandomFunction nonRandomFunction = (NonRandomFunction) funcAppTerm
        .getFunction();
    if (!(nonRandomFunction.getInterpretation() instanceof ConstantInterp))
      return null;
    ConstantInterp interp = (ConstantInterp) nonRandomFunction
        .getInterpretation();
    Object value = interp.getValue(Util.list());
    if (!(value instanceof Timestep))
      return null;
    return (Timestep) value;
    // Prime example of why dynamic typing sucks. :-) Rodrigo
  }

  private static class TimestepSelector implements UnaryPredicate {
    public boolean evaluate(Object term) {
      return isTimestep((ArgSpec) term);
    }
  };

  private static final TimestepSelector timestepSelector = new TimestepSelector();

  /**
   * Adds all timesteps in an ArgSpec to a collection and return that
   * collection.
   */
  public static Collection getTimestepTermsIn(ArgSpec argSpec,
      Collection timesteps) {
    argSpec.selectTerms(timestepSelector, timesteps);
    return timesteps;
  }

  private static class TimestepTermComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      return getTimestepInTimestepTerm((ArgSpec) o1).compareTo(
          getTimestepInTimestepTerm((ArgSpec) o2));
    }

    public boolean equals(Object obj) {
      return obj instanceof TimestepTermComparator;
    }
  };

  /**
   * Split evidence by the timestep they refer to.
   * Atemporal evidence is assigned to timestep null.
   */
  public static Map<Timestep, Evidence> splitEvidenceInTime(Evidence evidence) {
    // First we accumulate all statements for each timestep.
    Map<Timestep, List<Object>> table = new HashMap<Timestep, List<Object>>();
    for (ValueEvidenceStatement statement : evidence.getValueEvidence()) {
      // FIXME: timesteps actually accumulates ArgSpecs for which
      // getTimestepInTimestepTerm returns non-null. Quite confusing.
      TreeSet<ArgSpec> timesteps = new TreeSet(new TimestepTermComparator());
      getTimestepTermsIn(statement.getLeftSide(), timesteps);
      getTimestepTermsIn(statement.getOutput(), timesteps);
      Timestep maxTimestep = null;
      if (!timesteps.isEmpty()) {
        maxTimestep = getTimestepInTimestepTerm(timesteps.last());
      }
      List<Object> statements = table.get(maxTimestep);
      if (statements == null) {
        statements = new LinkedList<Object>();
        table.put(maxTimestep, statements);
      }
      statements.add(statement);
    }
    for (SymbolEvidenceStatement statement : evidence.getSymbolEvidence()) {
      // FIXME: timesteps actually accumulates ArgSpecs for which
      // getTimestepInTimestepTerm returns non-null. Quite confusing.
      TreeSet<ArgSpec> timesteps = new TreeSet(new TimestepTermComparator());
      getTimestepTermsIn(statement.getSetSpec(), timesteps);
      Timestep maxTimestep = null;
      if (!timesteps.isEmpty()) {
        maxTimestep = getTimestepInTimestepTerm(timesteps.last());
      }
      List<Object> statements = table.get(maxTimestep);
      if (statements == null) {
        statements = new LinkedList<Object>();
        table.put(maxTimestep, statements);
      }
      statements.add(statement);
    }
    // Then we convert each list of statements to an Evidence object.
    Map<Timestep, Evidence> result = new HashMap<Timestep, Evidence>();
    for (Map.Entry<Timestep, List<Object>> entry : table.entrySet()) {
      result
          .put(entry.getKey(), Evidence.constructAndCompile(entry.getValue()));
    }
    return result;
  }

  /**
   * Split queries by the timestep they refer to.
   * Atemporal queries are assigned to timestep null.
   */
  public static Map<Timestep, List<Query>> splitQueriesInTime(
      List<Query> queries) {
    Map<Timestep, List<Query>> table = new HashMap<Timestep, List<Query>>();
    for (Query q : queries) {
      Timestep t = getSingleTimestepIn(((ArgSpecQuery) q).argSpec());
      List<Query> qs = table.get(t);
      if (qs == null) {
        qs = new LinkedList<Query>();
        table.put(t, qs);
      }
      qs.add(q);
    }
    return table;
  }

  public static Evidence getEvidenceUpTo(int timestepIndex, Evidence evidence) {
    Map<Timestep, Evidence> splitEvidence = splitEvidenceInTime(evidence);
    final Evidence result = new Evidence();
    for (Map.Entry<Timestep, Evidence> entry : splitEvidence.entrySet()) {
      if (entry.getKey().intValue() <= timestepIndex) {
        result.addAll(entry.getValue());
      }
    }
    return result;
  }
}
