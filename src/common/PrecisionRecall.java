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

package common;

/**
 * An object of class PrecisionRecall represents precision and recall 
 * statistics for some experiment.  
 */
public class PrecisionRecall {
    /**
     * Creates a new PrecisionRecall object with the given precision and 
     * recall.
     */
    public PrecisionRecall(double precision, double recall) {
	this.precision = precision;
	this.recall = recall;
	computeF();
    }

    /**
     * Creates a new PrecisionRecall object for the given experimental 
     * results.
     *
     * @param numTrue       true number of objects with some desired property
     * @param numFound      number of objects found in this experiment
     * @param numTrueFound  number of objects found that really have 
     *                      the desired property
     */
    public PrecisionRecall(int numTrue, int numFound, int numTrueFound) {
	precision = numTrueFound / (double) numFound;
	recall = numTrueFound / (double) numTrue;
	computeF();
    }

    /**
     * Returns the precision, defined as: the number of objects found that 
     * have a desired property, divided by the total number of objects found.
     */
    public double getPrecision() {
	return precision;
    }

    /**
     * Returns the recall, defined as the number of objects found that 
     * have a desired property, divided by the total number of objects 
     * with that property.
     */
    public double getRecall() {
	return recall;
    }

    /**
     * Returns the F1 statistic, which is the harmonic mean of precision 
     * and recall: 1/F1 = 1/P + 1/R.
     */
    public double getF1() {
	return f1;
    }

    private void computeF() {
	// 1/F = (1/p + 1/r) / 2
	// F = 2pr / (p + r)
	f1 = 2 * precision * recall / (precision + recall);
    }

    public String toString() {
	return ("precision: " + precision + ", recall: " + recall 
		+ ", F1: " + f1);
    }

    double precision;
    double recall;
    double f1;
}
