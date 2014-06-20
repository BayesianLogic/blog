/*
 * Copyright (c) 2005, Regents of the University of California
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
 * * Neither the name of the University of California, Berkeley nor
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

package blog.distrib;

import blog.common.Util;

/**
 * Laplace distribution over real numbers. This CPD can be initialized
 * with exactly two parameters. If two parameters are given, then the
 * first is the mean and the second is the diversity.
 * 
 * @author leili
 * @author datang
 * @since 2014/06/11
 */
public class Laplace implements CondProbDistrib {

  /*
   * Laplace accepts an array of exactly two parameters (could be null)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2)
      throw new IllegalArgumentException(
          "Laplace distribution expect two parameters: mean and diversity");
    setParams((Double) params[0], (Double) params[1]);
  }

  /**
   * Set the parameters for Laplace distribution. This method is intended for
   * programmer use. For engine use, please see setParams(Object[] params)
   * 
   * @param mean
   *          the mean of the Laplace. The mean
   *          should be set at least once.
   * @param diversity
   *          the diversity of Laplace. The
   *          diversity should be set at least once.
   *          The diversity must be a positive real number.
   */
  public void setParams(Double mean, Double diversity) {
    if (mean != null) {
      hasMean = true;
      this.mean = mean;
    }
    if (diversity != null) {
      if (diversity <= 0) {
        throw new IllegalArgumentException(
            "Diversity of Laplace distribution must be positive, not "
                + diversity);
      }
      hasDiversity = true;
      this.diversity = diversity;
      normConst = diversity * 2;
      logNormConst = Math.log(diversity) + Math.log(2);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  /**
   * calculate the probability of a given value
   * The parameter must be set before calling this method.
   * Note this method is intended for human use. Please use getProb(Object) for
   * the engine use.
   * 
   * @param value
   * @return
   */
  public double getProb(double value) {
    checkHasParams();
    return (Math.exp(-Math.abs(value - mean) / diversity) / normConst);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  /**
   * calculate the log probability of the given value
   * 
   * @param value
   * @return
   */
  public double getLogProb(double value) {
    checkHasParams();
    return (-Math.abs(value - mean) / diversity) - logNormConst;
  }

  /**
   * Returns a value sampled from this Laplace distribution. This method should
   * only be called if the mean and diversity were set in the constructor
   * (internal calls are ok if the private method <code>initParams</code> is
   * called first).
   * 
   * <p>
   * The implementation treats a Laplace distribution as the combination of two
   * Exponential distributions. See
   * http://en.wikipedia.org/wiki/Laplace_distribution
   * #Generating_random_variables_according_to_the_Laplace_distribution
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    double U = Util.random() - 0.5;
    if (U > 0)
      return mean - diversity * Math.log(1 - 2 * U);
    else
      return mean + diversity * Math.log(1 + 2 * U);
  }

  /**
   * get the string representation of Laplace.
   */
  public String toString() {
    return "Laplace(" + mean + ", " + diversity + ")";
  }

  /**
   * check whether both parameters are set.
   */
  private void checkHasParams() {
    if (!hasMean)
      throw new IllegalArgumentException("mean of Laplace not provided");
    if (!hasDiversity)
      throw new IllegalArgumentException("diversity of Laplace not provided");
  }

  // parameters
  private boolean hasMean = false;
  private boolean hasDiversity = false;
  private double mean;
  private double diversity;

  // precomputed
  private double normConst;
  private double logNormConst;
}
