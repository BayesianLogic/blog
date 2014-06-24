package blog.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import blog.bn.BayesNetVar;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;

/**
 * Container for queries for a particular model.
 * 
 * @author cberzan
 * @since Jun 20, 2014
 */
public class Queries extends ArrayList<Query> {
  private static final long serialVersionUID = 1L;

  public Queries(Model model) {
    super();
    this.model = model;
  }

  public void addFromFile(String path) {
    addFromParse(Parse.parseFile(path));
  }

  public void addFromString(String string) {
    addFromParse(Parse.parseString(string));
  }

  public void addFromParse(Parse parse) {
    Evidence dummyEvidence = new Evidence(model);
    ErrorMsg dummyErr = new ErrorMsg("");
    Semant sem = new Semant(model, dummyEvidence, this, dummyErr);
    sem.transProg(parse.getResult());
  }

  public boolean checkTypesAndScope() {
    for (Query q : this) {
      if (!q.checkTypesAndScope(model)) {
        return false;
      }
    }
    return true;
  }

  public int compile() {
    int errors = 0;
    for (Query query : this) {
      query.compile();
    }
    return errors;
  }

  /**
   * @return Union of variables that are sufficient to answer the queries.
   */
  public Collection<? extends BayesNetVar> getVariables() {
    HashSet<BayesNetVar> result = new HashSet<BayesNetVar>();
    for (Query query : this) {
      result.addAll(query.getVariables());
    }
    return result;
  }

  // The model that these queries are for.
  final public Model model;
}
