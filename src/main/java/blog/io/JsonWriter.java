/**
 * 
 */
package blog.io;

import java.util.ArrayList;

import blog.common.Histogram;
import blog.model.ArgSpecQuery;
import blog.model.Query;

import com.google.gson.Gson;

/**
 * Write query results in JSON format.
 * 
 * This is a machine-readable output format. For every query, we output the a
 * list of (value, log_prob) pairs. The value is always a string obtained by
 * calling toString() on the corresponding Java object.
 * 
 * Example output (prettified to show structure):
 * 
 * <code>
 * [
 *     ["Damage(A)", [
 *         ["Mild", 2.639057329615258],
 *         ["Severe", 3.526360524616161]
 *     ]],
 *     ["Damage(B)", [
 *         ["Mild", 2.9957322735539904],
 *         ["Severe", 3.3322045101752034]
 *     ]]
 * ]
 * </code>
 * 
 * @author cberzan
 * @since Jun 9, 2014
 *
 * @author leili
 * @date Oct 8, 2014
 *       modified according to the new interface
 */
public class JsonWriter extends ResultWriter {

  public JsonWriter() {
    super();
    allResults = new ArrayList<Object>();
  }

  @Override
  public void writeResult(Query query) {
    Histogram histogram = query.getHistogram();
    ArrayList<Object> histogramEntries = new ArrayList<Object>();
    for (Object entry_obj : histogram.entrySet()) {
      Histogram.Entry entry = (Histogram.Entry) entry_obj;
      ArrayList<Object> entryPair = new ArrayList<Object>();
      entryPair.add(entry.getElement().toString());
      entryPair.add(entry.getLogWeight());
      histogramEntries.add(entryPair);
    }
    ArrayList<Object> results = new ArrayList<Object>();
    results.add(((ArgSpecQuery) query).getArgSpec().toString());
    results.add(histogramEntries);
    allResults.add(results);
  }

  @Override
  public void flush() {
    if (!allResults.isEmpty()) {
      Gson gson = new Gson();
      String json = gson.toJson(allResults);
      allResults.clear();
      out.println(json);
    }
  }

  protected ArrayList<Object> allResults;
}
