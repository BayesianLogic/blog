package blog.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import blog.common.Histogram;
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
 * Distribution of values for Burglary
 *  false 0.7233733088174801
 *  true  0.2766266911825274
 * 
 * ======== Done ========
 * </code>
 * 
 * @author cberzan
 * @since Jun 9, 2014
 * 
 * @author leili
 * @date Oct 8, 2014
 *       modified according to the new interface
 */
public class TableWriter extends ResultWriter {

  public TableWriter() {
  }

  @Override
  public void writeResult(Query query) {
    ArgSpecQuery q = (ArgSpecQuery) query;
    ArgSpec spec = q.getArgSpec();
    Histogram histogram = query.getHistogram();
    out.println("Distribution of values for " + spec);
    List<?> entries = new ArrayList(histogram.entrySet());

    if (spec.isNumeric())
      Collections.sort(entries, NUMERIC_COMPARATOR);
    else
      Collections.sort(entries, WEIGHT_COMPARATOR);

    for (Iterator<?> iter = entries.iterator(); iter.hasNext();) {
      Histogram.Entry entry = (Histogram.Entry) iter.next();
      double prob = histogram.getProb(entry.getElement());
      out.print("\t");
      out.print(entry.getElement());
      out.print("\t");
      out.println(prob);
    }
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
