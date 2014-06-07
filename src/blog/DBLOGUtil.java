package blog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.FilteredIterator;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
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
      if (timestepIndex < 0) {
        return null;
      }
      alreadyReturned.add(timestepIndex);
      return timestepIndex;
    }

    private HashSet<Integer> alreadyReturned = new HashSet<Integer>();
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

  /**
   * Split evidence by the timestep they refer to.
   * Atemporal evidence is assigned to timestep null.
   */
  public static Map<Timestep, Evidence> splitEvidenceInTime(Evidence evidence) {
    // First we accumulate all statements for each timestep.
    Map<Timestep, List<Object>> table = new HashMap<Timestep, List<Object>>();
    for (ValueEvidenceStatement statement : evidence.getValueEvidence()) {
      Timestep maxTimestep = statement.getLeftSide().maxTimestep();
      List<Object> statements = table.get(maxTimestep);
      if (statements == null) {
        statements = new LinkedList<Object>();
        table.put(maxTimestep, statements);
      }
      statements.add(statement);
    }
    for (SymbolEvidenceStatement statement : evidence.getSymbolEvidence()) {
      Timestep maxTimestep = statement.getSetSpec().maxTimestep();
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
      Timestep t = ((ArgSpecQuery) q).argSpec().maxTimestep();
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
