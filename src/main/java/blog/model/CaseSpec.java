/**
 * 
 */
package blog.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import blog.common.UnaryProcedure;
import blog.sample.EvalContext;

/**
 * @author Yi Wu
 * @since 2014/6/26/
 * 
 */
public class CaseSpec extends ArgSpec {
  ArgSpec test;
  MapSpec clause; // map from any value to ArgSpec
  boolean compiled = false;

  /*
   * if this flag is true, the return value must be a term;
   * otherwise, the return value must be DistribSpec
   */
  boolean isInFixedFuncBody = false;
  /*
   * if this flag is true, we make sure that the return value
   * of this CaseSpec will be a DistribSpec
   */
  boolean isInRandomFuncBody = true;

  public void setInFixedFuncBody(boolean flag) {
    isInFixedFuncBody = flag;
  }

  public void setInRandomFuncBody(boolean flag) {
    isInRandomFuncBody = flag;
  }

  public CaseSpec(ArgSpec test, MapSpec clause) {
    this.test = test;
    this.clause = clause;
    compiled = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#evaluate(blog.sample.EvalContext)
   */
  @Override
  public Object evaluate(EvalContext context) {
    Object t = test.evaluate(context);
    if (t == null) {
      return null;
    }
    Map<Object, Object> mp = clause.evaluate(context);
    if (mp.containsKey(t)) {
      Object ret = mp.get(t);
      if (ret instanceof ArgSpec) // lazy evaluate
        return ((ArgSpec) ret).evaluate(context);
      return ret;
    } else
      return Model.NULL;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#containsRandomSymbol()
   */
  @Override
  public boolean containsRandomSymbol() {
    return test.containsRandomSymbol() || clause.containsRandomSymbol();
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#checkTypesAndScope(blog.model.Model, java.util.Map)
   */
  @Override
  public boolean checkTypesAndScope(Model model, Map scope) {
    return test.checkTypesAndScope(model, scope)
        && clause.checkTypesAndScope(model, scope);
  }

  public int compile(LinkedHashSet callStack) {
    callStack.add(this);
    int errors = test.compile(callStack) + clause.compile(callStack);
    compiled = true;

    if (!isInRandomFuncBody) {
      if (isInFixedFuncBody && this.containsRandomSymbol()) {
        System.err
            .println("Case Expression in Fixed Function CANNOT contain random elements!");
        errors++;
      }
    } else
      // in Random Function Body
      // make sure every branch will return a distribution spec
      // namely: for each branch of Term, generate a EqualsCPD distribution spec
      errors += clause.enforceDistribSpec();

    callStack.remove(this);
    return errors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#find(blog.model.Term)
   */
  @Override
  public ArgSpec find(Term t) {
    if ((test instanceof Term) && test.equals(t))
      return test;
    ArgSpec ret = test.find(t);
    if (ret != null)
      return ret;
    return clause.find(t);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#applyToTerms(blog.common.UnaryProcedure)
   */
  @Override
  public void applyToTerms(UnaryProcedure procedure) {
    test.applyToTerms(procedure);
    clause.applyToTerms(procedure);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#replace(blog.model.Term, blog.model.ArgSpec)
   */
  @Override
  public ArgSpec replace(Term t, ArgSpec another) {
    ArgSpec newTest = test.replace(t, another);
    MapSpec newClause = (MapSpec) clause.replace(t, another);
    if (newTest == test && newClause == clause)
      return this;
    CaseSpec newCase = new CaseSpec(newTest, newClause);
    if (compiled)
      newCase.compile(new LinkedHashSet());
    return newCase;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.ArgSpec#getSubstResult(blog.model.Substitution,
   * java.util.Set)
   */
  @Override
  public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
    return new CaseSpec(test.getSubstResult(subst, boundVars),
        (MapSpec) clause.getSubstResult(subst, boundVars));
  }

  public String toString() {
    return "case " + test + " in " + clause;
  }
}
