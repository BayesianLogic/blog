package blog.model;

/**
 * A convenience container for a Model, Evidence, and Queries.
 * 
 * Invariant: The Evidence and Queries objects point to the Model.
 * 
 * @author cberzan
 * @author unknown
 */
public class ModelEvidenceQueries {
  public ModelEvidenceQueries(Model model, Evidence evidence, Queries queries) {
    // Verify invariant.
    assert (evidence.model == model);
    assert (queries.model == model);

    this.model = model;
    this.evidence = evidence;
    this.queries = queries;
  }

  // These are final to avoid accidentally breaking the invariant.
  final public Model model;
  final public Evidence evidence;
  final public Queries queries;
}
