/*
 * Copyright (c) 2008 Massachusetts Institute of Technology
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the Massachusetts Institute of Technology nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fove;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ve.MultiArrayPotential;
import blog.ObjectIdentifier;
import blog.bn.BasicVar;
import blog.bn.RandFuncAppVar;
import blog.distrib.CondProbDistrib;
import blog.distrib.DetCondProbDistrib;
import blog.model.ArgSpec;
import blog.model.CardinalitySpec;
import blog.model.Clause;
import blog.model.ConjFormula;
import blog.model.DependencyModel;
import blog.model.Formula;
import blog.model.FuncAppTerm;
import blog.model.ImplicitSetSpec;
import blog.model.LogicalVar;
import blog.model.RandomFunction;
import blog.model.Substitution;
import blog.model.Term;
import blog.model.Type;
import blog.sample.EvalContext;
import blog.sample.ParentRecEvalContext;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * A decision tree where the internal nodes split on the values of terms (which
 * may contain logical variables).
 */
public class LiftedDecisionTree extends ve.DecisionTree<Term> {
  /**
   * Returns a decision tree corresponding to the dependency model for the given
   * random function.
   * 
   * @param a
   *          Assignment of values to a subset of the logical variables that
   *          stand for <code>f</code>'s arguments. The resulting decision tree
   *          will apply to all applications of the function where the specified
   *          arguments have the specified values.
   * 
   * @param childAtom
   *          Term to split on at the last level of the tree representing the
   *          value of the child random variable, or null to indicate that the
   *          distributions will be deterministic and the tree should not split
   *          on the child variable (in which case the leaf values will be
   *          values of the child variable, not probabilities).
   */
  public static LiftedDecisionTree createForFunc(RandomFunction rf,
      Map<LogicalVar, Object> a, FuncAppTerm childAtom) {
    // Create an empty partial world
    PartialWorld w = new DefaultPartialWorld();
    ParentRecEvalContext context = new ParentRecEvalContext(w);

    // Bind argument variables to values. If a variable's value is not
    // specified by the assignment a, create an ObjectIdentifier for it.
    // Record the map from ObjectIdentifiers back to logical variables
    // so we can convert from parent random variables to terms later.
    Map<ObjectIdentifier, LogicalVar> skolemMap = new HashMap<ObjectIdentifier, LogicalVar>();
    LogicalVar[] argVars = rf.getArgVars();
    for (int i = 0; i < argVars.length; ++i) {
      Object val = a.get(argVars[i]);
      if (val == null) {
        ObjectIdentifier skolem = new ObjectIdentifier(argVars[i].getType());
        skolemMap.put(skolem, argVars[i]);
        val = skolem;
      }
      context.assign(argVars[i], val);
    }

    Node root = createSubtreeForContext(rf, w, context, skolemMap, childAtom);
    return new LiftedDecisionTree(root);
  }

  /**
   * Returns a MultiArrayPotential defining the same function as this decision
   * tree.
   * 
   * @param atoms
   *          list of atoms corresponding to the dimensions of the potential to
   *          be returned. Must include all the atoms that are split on in this
   *          decision tree.
   */
  public MultiArrayPotential getMultiArray(List<Term> atoms) {
    List<Type> retTypes = new ArrayList<Type>();
    for (Term atom : atoms) {
      retTypes.add(atom.getType());
    }

    MultiArrayPotential pot = new MultiArrayPotential(retTypes);
    fillMultiArray(pot, atoms);
    return pot;
  }

  private static Node createSubtreeForContext(RandomFunction rf,
      PartialWorld w, ParentRecEvalContext context,
      Map<ObjectIdentifier, LogicalVar> skolemMap, FuncAppTerm childAtom) {
    // TODO: get EqualityFormula.evaluate to handle
    // ObjectIdentifiers correctly in situations like this
    Clause activeClause = rf.getDepModel().getActiveClause(context);
    if (activeClause == null) {
      // world is not complete enough to determine active clause
      BasicVar parentVar = context.getLatestUninstParent();
      if (parentVar == null) {
        throw new IllegalStateException(
            "Active clause was null, but no uninstantiated parent.");
      }

      // split on parent var
      InternalNode node = new InternalNode();
      node.setSplitLabel(parentVar.getCanonicalTerm(skolemMap));

      for (Object val : parentVar.getType().range()) {
        w.setValue(parentVar, val);
        Node child = createSubtreeForContext(rf, w, context, skolemMap,
            childAtom);
        node.setChildForValue(child, val);
      }
      w.setValue(parentVar, null);
      return node;
    }

    // Context determines the active clause
    List argValuesSoFar = new ArrayList<Object>();
    return createSubtreeForArgs(rf, w, context, activeClause, argValuesSoFar,
        skolemMap, childAtom);
  }

  private static Node createSubtreeForArgs(RandomFunction rf, PartialWorld w,
      ParentRecEvalContext context, Clause clause, List<Object> argValuesSoFar,
      Map<ObjectIdentifier, LogicalVar> skolemMap, FuncAppTerm childAtom) {
    List args = clause.getArgsNonFixed();
    if (argValuesSoFar.size() == args.size()) {
      // done finding argument values
      DependencyModel.Distrib distrib = new DependencyModel.Distrib(
          clause.getCPD(), argValuesSoFar);
      return createSubtreeForDistrib(rf, distrib, skolemMap, childAtom);
    }

    ArgSpec arg = (ArgSpec) args.get(argValuesSoFar.size());
    Object value = arg.evaluate(context);
    if (value == null) {
      // world is not complete enough to determine argument value
      BasicVar parentVar = context.getLatestUninstParent();
      if (parentVar == null) {
        throw new IllegalStateException(
            "Arg value was null, but no uninstantiated parent.");
      }

      if (arg instanceof CardinalitySpec) {
        Node child = createSubtreeForArgsFirstCount(rf, w, context, clause,
            argValuesSoFar, (CardinalitySpec) arg, skolemMap, childAtom);
        if (child != null) {
          return child;
        }
      }

      // split on parent var
      InternalNode node = new InternalNode();
      node.setSplitLabel(parentVar.getCanonicalTerm(skolemMap));

      for (Object val : parentVar.getType().range()) {
        w.setValue(parentVar, val);
        Node child = createSubtreeForArgs(rf, w, context, clause,
            argValuesSoFar, skolemMap, childAtom);
        node.setChildForValue(child, val);
      }
      w.setValue(parentVar, null);
      return node;
    }

    // Context determines the value of this argument
    argValuesSoFar.add(value);
    Node result = createSubtreeForArgs(rf, w, context, clause, argValuesSoFar,
        skolemMap, childAtom);
    argValuesSoFar.remove(argValuesSoFar.size() - 1);
    return result;
  }

  private static Node createSubtreeForArgsFirstCount(RandomFunction rf,
      PartialWorld w, ParentRecEvalContext context, Clause clause,
      List<Object> argValuesSoFar, CardinalitySpec cardSpec,
      Map<ObjectIdentifier, LogicalVar> skolemMap, FuncAppTerm childAtom) {
    ImplicitSetSpec setSpec = cardSpec.getSetSpec();
    LogicalVar x = setSpec.getGenericSetElt();

    // Make sure we can count over the type
    Type type = setSpec.getType();
    if (!(type.hasFiniteGuaranteed() && type.getPOPs().isEmpty())) {
      return null;
    }

    // Get the CNF form of the formula in the cardinality expression.
    // Make sure that each conjunct either is nonrandom, or contains
    // the quantified variable x only in a single fixed func app.
    ConjFormula cnf = setSpec.getCond().getPropCNF();
    List<Formula> nonRandomConjuncts = new ArrayList<Formula>();
    List<Formula> randomConjuncts = new ArrayList<Formula>();
    for (Iterator iter = cnf.getConjuncts().iterator(); iter.hasNext();) {
      Formula conjunct = (Formula) iter.next();
      if (conjunct.containsRandomSymbol()) {
        randomConjuncts.add(conjunct);
      } else {
        nonRandomConjuncts.add(conjunct);
      }
    }
    FuncAppTerm targetFuncApp = getSingleFuncAppUsing(x, new ConjFormula(
        randomConjuncts));
    targetFuncApp = getTargetVersion(targetFuncApp, context, x, skolemMap);
    if (targetFuncApp == null) {
      return null;
    }

    // Make constraint from non-random conjuncts
    Set<LogicalVar> xSet = Collections.singleton(x);
    ConjFormula constraintFormula = new ConjFormula(nonRandomConjuncts);
    if (!xSet.containsAll(constraintFormula.getFreeVars())) {
      // We don't allow constraints with other logical variables,
      // because then we'd have to worry about the constraint being
      // jointly in normal form with the constraint on the other
      // logical variables (to compute the set of possible histograms).
      return null;
    }
    Constraint constraint = null;
    try {
      constraint = new Constraint(constraintFormula, xSet);
    } catch (IllegalArgumentException e) {
      return null; // can't convert to constraint
    }

    // Figure out which values of targetFuncApp satisfy the formula.
    BitSet satIndices = getValuesThatSatisfy(targetFuncApp, new ConjFormula(
        randomConjuncts), w, context, x);
    if (satIndices == null) {
      // There are some other random variables whose values are
      // needed. We could deal with this by splitting on those
      // variables, but for now we just don't create a counting term.
      return null;
    }

    // Make the counting term and the node
    List<FuncAppTerm> subTerms = Collections.singletonList(targetFuncApp);
    CountingTerm ct = new CountingTerm(xSet, constraint, subTerms);
    InternalNode node = new InternalNode();
    node.setSplitLabel(ct);

    // Add a child for each histogram. When constructing the subtree
    // for each child, fill in the next argument value with the
    // cardinality expression value resulting from the histogram.
    List<Type> dims = Collections.singletonList(targetFuncApp.getType());
    Type histType = new HistogramType(dims,
        constraint.numConstrainedGroundings(x));
    for (Object hist : histType.range()) {
      int numSatisfiers = 0;
      for (int i = satIndices.nextSetBit(0); i >= 0; i = satIndices
          .nextSetBit(i + 1)) {
        numSatisfiers += ((Histogram) hist).getCount(i);
      }

      argValuesSoFar.add(numSatisfiers);
      Node child = createSubtreeForArgs(rf, w, context, clause, argValuesSoFar,
          skolemMap, childAtom);
      node.setChildForValue(child, hist);
      argValuesSoFar.remove(argValuesSoFar.size() - 1);
    }

    return node;
  }

  // Assumes x is used somewhere in the given formula.
  private static FuncAppTerm getSingleFuncAppUsing(LogicalVar x, Formula formula) {
    FuncAppTerm toReturn = null;

    for (Iterator iter = formula.getSubformulas().iterator(); iter.hasNext();) {
      Formula subForm = (Formula) iter.next();
      if (subForm.getFreeVars().contains(x)) {
        FuncAppTerm funcApp = getSingleFuncAppUsing(x, subForm);
        if (funcApp == null) {
          // sub-formula uses x outside a single func app
          return null;
        }
        if (toReturn == null) {
          toReturn = funcApp;
        } else if (!funcApp.equals(toReturn)) {
          return null; // different func app from earlier
        }
      }
    }

    for (Iterator iter = formula.getTopLevelTerms().iterator(); iter.hasNext();) {
      Term term = (Term) iter.next();
      if (term.getFreeVars().contains(x)) {
        if (term instanceof FuncAppTerm) {
          if (toReturn == null) {
            toReturn = (FuncAppTerm) term;
          } else if (!term.equals(toReturn)) {
            return null; // different func app from earlier
          }
        } else {
          return null; // used in non-func-app term
        }
      }
    }

    return toReturn;
  }

  private static FuncAppTerm getTargetVersion(FuncAppTerm funcApp,
      EvalContext context, LogicalVar allowedFreeVar,
      Map<ObjectIdentifier, LogicalVar> skolemMap) {
    if (funcApp == null) {
      return null;
    }

    Type type = funcApp.getType();
    if (!(type.hasFiniteGuaranteed() && type.getPOPs().isEmpty())) {
      return null; // can't count over values of this function
    }

    Set<LogicalVar> freeVars = funcApp.getFreeVars();
    for (LogicalVar y : freeVars) {
      if (y == allowedFreeVar) {
        continue;
      }
      Object val = context.getLogicalVarValue(y);
      if (val == null) {
        return null; // extra free variable
      }

      if (!skolemMap.containsKey(val)) {
        Term term = y.getType().getCanonicalTerm(val);
        Substitution subst = new Substitution();
        subst.add(y, term);
        funcApp = (FuncAppTerm) funcApp.getSubstResult(subst);
      }
    }

    funcApp = (FuncAppTerm) funcApp.getCanonicalVersion();
    ArgSpec[] args = funcApp.getArgs();
    for (int i = 0; i < args.length; ++i) {
      ArgSpec arg = args[i];
      if ((arg instanceof FuncAppTerm)
          && (((FuncAppTerm) arg).getArgs().length > 0)) {
        return null; // func app is nested
      }
    }

    return funcApp;
  }

  private static BitSet getValuesThatSatisfy(FuncAppTerm targetFuncApp,
      Formula formula, PartialWorld w, EvalContext context, LogicalVar varToBind) {
    // Bind variable to an ObjectIdentifier
    ObjectIdentifier skolem = new ObjectIdentifier(varToBind.getType());
    context.assign(varToBind, skolem);

    // Create random variable
    Term[] args = (Term[]) targetFuncApp.getArgs();
    Object[] argValues = new Object[args.length];
    for (int i = 0; i < args.length; ++i) {
      Object argVal = args[i].evaluate(context);
      if (argVal == null) {
        context.unassign(varToBind);
        return null; // arg is random
      }
      argValues[i] = argVal;
    }
    BasicVar rv = new RandFuncAppVar(
        (RandomFunction) targetFuncApp.getFunction(), argValues, true);

    // Figure out which values satisfy formula
    List<Object> range = targetFuncApp.getType().range();
    BitSet satIndices = new BitSet(range.size());
    int i = 0;
    for (Object val : range) {
      w.setValue(rv, val);
      Boolean truth = (Boolean) formula.evaluate(context);
      if (truth == null) {
        satIndices = null;
        break;
      }

      satIndices.set(i++, truth.booleanValue());
    }
    w.setValue(rv, null);

    context.unassign(varToBind);
    return satIndices;
  }

  private static Node createSubtreeForDistrib(RandomFunction rf,
      DependencyModel.Distrib distrib,
      Map<ObjectIdentifier, LogicalVar> skolemMap, FuncAppTerm childAtom) {
    if (childAtom == null) {
      Leaf node = new Leaf();
      Object val = ((DetCondProbDistrib) distrib.getCPD())
          .getChildValue(distrib.getArgValues());
      node.setWeight(((Number) val).doubleValue());
      return node;
    }

    // split on child atom
    InternalNode node = new InternalNode();
    node.setSplitLabel(childAtom);

    // Create a leaf for each child value, label it with probability
    for (Object childValue : rf.getRetType().range()) {
      Leaf child = new Leaf();
      node.setChildForValue(child, childValue);
      CondProbDistrib cpd = distrib.getCPD();
      cpd.setParams(distrib.getArgValues());
      double prob = cpd.getProb(childValue);
      child.setWeight(prob);
    }

    return node;
  }

  /**
   * Creates a new LiftedDecisionTree with the given root node. External clients
   * should call <code>createForFunc</code> instead.
   */
  protected LiftedDecisionTree(Node root) {
    super(root);
  }
}
