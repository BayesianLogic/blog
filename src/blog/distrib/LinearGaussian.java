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

import blog.*;
import blog.model.Type;

import java.util.*;
import Jama.*;

/**
 * Multivariate Gaussian distribution whose mean is an affine function of some
 * vector-valued parents. Specifically, the distribution is Normal(mu + W v,
 * Sigma). Here v is a column vector formed by stacking all the parent vectors
 * on top of each other; we'll call its dimension c. Then mu is a 1xd mean
 * vector, W is a dxc regression matrix, and Sigma is a dxd covariance matrix.
 * Note that the covariance of this distribution does not depend on the parents.
 * 
 * <p>
 * In an input file, this CPD takes three parameters: mu, W, Sigma.
 */
public class LinearGaussian extends AbstractCondProbDistrib {
	/**
	 * Takes a parameter list whose first element is a 1xd mean matrix, second
	 * element is a dxc regression matrix (where c is the sum of the dimensions of
	 * the parent vectors), and third element is a dxd covariance matrix.
	 */
	public LinearGaussian(List params) {
		if (params.size() != 3) {
			throw new IllegalArgumentException(
					"LinearGaussian CPD takes three parameters: mu, W, Sigma.");
		}

		for (int i = 0; i < 3; ++i) {
			if (!(params.get(i) instanceof Matrix)) {
				throw new IllegalArgumentException(
						"All parameters to LinearGaussian CPD should be " + "matrices.");
			}
		}

		mu = (Matrix) params.get(0);
		if (mu.getColumnDimension() != 1) {
			throw new IllegalArgumentException(
					"First parameter to LinearGaussian CPD (mean mu) should "
							+ "be a column vector.");
		}
		d = mu.getRowDimension();

		W = (Matrix) params.get(1);
		if (W.getRowDimension() != d) {
			throw new IllegalArgumentException(
					"Second parameter to LinearGaussian CPD (weight matrix W) "
							+ "should have same number of rows as first parameter.");
		}
		c = W.getColumnDimension();

		Sigma = (Matrix) params.get(2);
		if (!((Sigma.getRowDimension() == d) && (Sigma.getColumnDimension() == d))) {
			throw new IllegalArgumentException(
					"Third parameter to LinearGaussian CPD (covariance matrix "
							+ "sigma) should be square matrix of same dimension as "
							+ "first parameter.");
		}

		// Make sure Sigma is symmetric
		for (int i = 0; i < Sigma.getRowDimension(); ++i) {
			for (int j = 0; j < i; ++j) {
				if (Sigma.get(i, j) != Sigma.get(j, i)) {
					throw new IllegalArgumentException(
							"Covariance matrix for LinearGaussian CPD must "
									+ "be symmetric.");
				}
			}
		}
	}

	public double getProb(List args, Object value) {
		Matrix v = getParentVector(args);

		if (value instanceof Matrix) {
			MultivarGaussian distrib = new MultivarGaussian(mu.plus(W.times(v)),
					Sigma);
			return distrib.getProb((Matrix) value);
		}
		return 0;
	}

	public Object sampleVal(List args, Type childType) {
		Matrix v = getParentVector(args);
		MultivarGaussian distrib = new MultivarGaussian(mu.plus(W.times(v)), Sigma);
		return distrib.sampleVal();
	}

	private Matrix getParentVector(List args) {
		Matrix v = new Matrix(c, 1);
		int curRow = 0;
		for (Iterator iter = args.iterator(); iter.hasNext();) {
			Object arg = iter.next();
			if (!((arg instanceof Matrix) && (((Matrix) arg).getColumnDimension() == 1))) {
				throw new IllegalArgumentException(
						"Arguments for LinearGaussian CPD must be " + "column vectors.");
			}

			Matrix parent = (Matrix) arg;
			if (curRow + parent.getRowDimension() > c) {
				throw new IllegalArgumentException(
						"Error in LinearGaussian CPD: sum of dimensions of "
								+ "parents exceeds number of columns in W.");
			}
			v.setMatrix(curRow, curRow + parent.getRowDimension() - 1, 0, 0, parent);
			curRow += parent.getRowDimension();
		}

		if (curRow < c) {
			throw new IllegalArgumentException(
					"Error in LinearGaussian CPD: sum of dimensions of "
							+ "parents is less than number of columns in W.");
		}

		return v;
	}

	private int d; // dimension of child vector
	private int c; // sum of dimensions of parent vectors

	private Matrix mu;
	private Matrix W;
	private Matrix Sigma;
}
