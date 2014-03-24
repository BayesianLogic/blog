/*
 * Copyright (c) 2005, 2006, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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

package blog.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ve.Factor;
import ve.Potential;
import blog.Main;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.UnaryFunction;
import blog.world.PartialWorld;

/**
 * A query on the value of a given {@link ArgSpec}.
 * 
 * The class accepts a normalizer, that is, a UnaryFunction mapping each
 * possible value to a representative in a user-defined equivalency class. The
 * representative is the one stored in the query's histogram. By default, the
 * normalizer is the identity function.
 */
public class ArgSpecQuery extends AbstractQuery {

  public ArgSpecQuery(ArgSpec argSpec) {
    this.argSpec = argSpec;
  }

  public ArgSpecQuery(ArgSpecQuery another) {
    this(another.argSpec());
    if (another.getNormalizer() != null)
      this.setNormalizer(another.getNormalizer());
    if (another.variable != null)
      compile(); // if another is compiled, compile this one too.
  }

  public ArgSpec argSpec() {
    return getArgSpec();
  }

  public void printResults(PrintStream s) {
    s.println("Distribution of values for " + getArgSpec());
    List entries = new ArrayList(histogram.entrySet());

    if (getArgSpec().isNumeric())
      Collections.sort(entries, NUMERIC_COMPARATOR);
    else
      Collections.sort(entries, WEIGHT_COMPARATOR);

    for (Iterator iter = entries.iterator(); iter.hasNext();) {
      Histogram.Entry entry = (Histogram.Entry) iter.next();
      double prob = histogram.getProb(entry.getElement());
      s.println("\t" + prob + "\t" + entry.getElement());
    }
  }

  public Collection<? extends BayesNetVar> getVariables() {
    if (variable == null) {
      throw new IllegalStateException("Query has not yet been compiled.");
    }
    return Collections.singleton(variable);
  }

  public BayesNetVar getVariable() {
    if (variable == null) {
      throw new IllegalStateException("Query has not yet been compiled.");
    }
    return variable;
  }

  public boolean checkTypesAndScope(Model model) {
    if (getArgSpec() instanceof Term) {
      Term termInScope = ((Term) getArgSpec()).getTermInScope(model,
          Collections.EMPTY_MAP);
      if (termInScope == null) {
        return false;
      }
      argSpec = termInScope;
      return true;
    }
    return getArgSpec().checkTypesAndScope(model, Collections.EMPTY_MAP);
  }

  /**
   * Compiles the underlying ArgSpec, and initializes the variable corresponding
   * to this query.
   */
  public int compile() {
    int errors = getArgSpec().compile(new LinkedHashSet());
    if (errors == 0) {
      variable = getArgSpec().getVariable();
    }
    return errors;
  }

  public void updateStats(PartialWorld world, double logWeight) {
    Object value = getArgSpec().evaluate(world);
    histogram.increaseWeight(value, logWeight);
  }

  public void setPosterior(Factor posterior) {
    if (!posterior.getRandomVars().contains((BasicVar) variable)) {
      throw new IllegalArgumentException("Query variable " + variable
          + " not covered by factor on " + posterior.getRandomVars());
    }
    if (posterior.getRandomVars().size() > 1) {
      throw new IllegalArgumentException("Answer to query on " + variable
          + " should be factor on " + "that variable alone, not "
          + posterior.getRandomVars());
    }

    Potential pot = posterior.getPotential();
    Type type = pot.getDims().get(0);
    histogram.clear();
    for (Object o : type.getGuaranteedObjects()) {
      histogram.increaseWeight(o, pot.getValue(Collections.singletonList(o)));
    }
  }

  public Histogram getHistogram() {
    return histogram;
  }

  public Collection elementSet() {
    return getHistogram().elementSet();
  }

  public double getProb(Object entry) {
    return getHistogram().getProb(entry);
  }

  /**
   * Returns a collection with up to <code>n</code> entries, and with the
   * minimum number of elements comprising at least <code>percentile</code> of
   * total mass.
   */
  public Collection getNBestButInUpper(int n, double percentile) {
    return getHistogram().getNBestButInUpper(n, percentile);
  }

  public Object getLocation() {
    return getArgSpec().getLocation();
  }

  /**
   * Remove entries from histogram such that at least
   * <code>(1 - percentile)</code> of the total weight remains.
   */
  public void prune(double percentile) {
    histogram.prune(percentile);
  }

  public UnaryFunction getNormalizer() {
    return histogram.getNormalizer();
  }

  public void setNormalizer(UnaryFunction normalizer) {
    histogram.setNormalizer(normalizer);
  }

  public String toString() {
    if (variable == null) {
      return getArgSpec().toString();
    }
    return variable.toString();
  }

  /**
   * @return the argSpec
   */
  public ArgSpec getArgSpec() {
    return argSpec;
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
  protected ArgSpec argSpec;
  protected BayesNetVar variable;
  protected Histogram histogram = new Histogram();
}
