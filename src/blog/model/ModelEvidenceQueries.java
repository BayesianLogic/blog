/**
 * 
 */
package blog.model;

import blog.common.Util;

public class ModelEvidenceQueries {
  public ModelEvidenceQueries() {
    model = new Model();
    evidence = new Evidence(model);
    queries = new Queries(model);
  }

  public ModelEvidenceQueries(Model model, Evidence evidence, Queries queries) {
    this.model = model;
    this.evidence = evidence;
    this.queries = queries;
  }

  /**
   * Convenience method assuming there is only one query, of class ArgSpecQuery,
   * and returning it.
   */
  public ArgSpecQuery argSpecQuery() {
    return (ArgSpecQuery) Util.getFirst(queries);
  }

  public Model model;
  public Evidence evidence;
  public Queries queries;
}
