package blog.io;

import java.io.PrintStream;
import java.util.Collection;

import blog.model.Query;

/**
 * Write query results in table format.
 * 
 * This is a human-readable output format. For each query, we output a list of
 * values the query may take, prefixed by their probabilities.
 * 
 * Example output:
 * 
 * <code>
 * ======== Query Results =========
 * Iteration: 10
 * Distribution of values for First
 *   0.49999999999999994 A
 *   0.49999999999999994 B
 * Distribution of values for Damage(A)
 *   0.6666666666666666  Severe
 *   0.33333333333333337 Mild
 * Distribution of values for Damage(B)
 *   0.49999999999999994 Mild
 *   0.49999999999999994 Severe
 * Distribution of values for Damage(NotFirst)
 *   0.8333333333333333  Mild
 *   0.16666666666666669 Severe
 * ======== Done ========
 * </code>
 * 
 * @author cberzan
 * @since Jun 9, 2014
 */
public class TableWriter extends ResultWriter {
  public TableWriter(Collection<Query> queries) {
    super(queries);
  }

  @Override
  public void writeResults(PrintStream stream) {
    stream.println("======== Query Results =========");
    if (header != null) {
      stream.println(header);
    }
    for (Query query : queries) {
      query.printResults(System.out);
      // TODO move method here
    }
    stream.println("======== Done ========");
    stream.println();
  }
}
