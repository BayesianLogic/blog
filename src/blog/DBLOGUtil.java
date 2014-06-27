package blog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import blog.bn.BasicVar;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
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
 * @author cberzan
 */
public class DBLOGUtil {
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
}
