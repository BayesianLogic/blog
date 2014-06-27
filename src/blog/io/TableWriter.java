package blog.io;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import blog.common.Histogram;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Model;
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
  public TableWriter(final Collection<Query> queries) {
    super(queries);
  }

  public TableWriter(final Query query) {
    super(query);
  }

  @Override
  public void writeResults(PrintStream stream) {
    if (header != null) {
      stream.println(header);
    }
    for (Query abstractQuery : queries) {
      if (abstractQuery instanceof ArgSpecQuery) {
        ArgSpecQuery query = (ArgSpecQuery) abstractQuery;
        ArgSpec spec = query.getArgSpec();
        Histogram histogram = query.getHistogram();
        stream.println("Distribution of values for " + spec);
        List entries = new ArrayList(histogram.entrySet());

        if (spec.isNumeric())
          Collections.sort(entries, NUMERIC_COMPARATOR);
        else
          Collections.sort(entries, WEIGHT_COMPARATOR);

        for (Iterator iter = entries.iterator(); iter.hasNext();) {
          Histogram.Entry entry = (Histogram.Entry) iter.next();
          double prob = histogram.getProb(entry.getElement());
          stream.println("\t" + prob + "\t" + entry.getElement());
        }
      } else {
        Util.fatalError("Don't know how to print Query of type "
            + abstractQuery.getClass());
      }
    }
    stream.println();
  }

  private static Comparator WEIGHT_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      double diff = (((Histogram.Entry) o1).getLogWeight() - ((Histogram.Entry) o2)
          .getLogWeight());
      if (diff < 0) {
        return 1;
      } else if (diff > 0) {
        return -1;
      }
      return 0;
    }
  };

  private static final Comparator NUMERIC_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      Object e1 = ((Histogram.Entry) o1).getElement();
      Object e2 = ((Histogram.Entry) o2).getElement();
      if (e1 == Model.NULL) {
        return -1;
      } else if (e2 == Model.NULL) {
        return 1;
      }
      double n1 = ((Number) e1).doubleValue();
      double n2 = ((Number) e2).doubleValue();
      if (n1 < n2) {
        return -1;
      } else if (n1 > n2) {
        return 1;
      }
      return 0;
    }
  };
}
