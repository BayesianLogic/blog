/**
 * 
 */
package blog.model;


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

  public Model model;
  public Evidence evidence;
  public Queries queries;
}
