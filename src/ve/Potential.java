/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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

package ve;

import blog.Type;
import java.util.List;
import java.io.PrintStream;

/**
 * 
 * Represents a mapping from tuples of objects to non-negative real numbers.
 * More specifically, each Potential has an associated tuple of Type objects
 * called its <i>dimensions</i>. It defines values for tuples of objects in the
 * Cartesian product of these types.
 */
public interface Potential {

	/**
	 * Returns the potential obtained by multiplying each entry in this potential
	 * by the corresponding entry in the given second potential.
	 */
	Potential multiply(Potential second, DimMapping second_product_map);

	/**
	 * Returns the potential obtained by summing out the given dimensions.
	 */
	Potential sumOut(List<Integer> sum_points);

	/**
	 * Returns the potential obtained by summing out those dimensions
	 * <code>i</code> for which <code>sum_out_mask[i]</code> is true.
	 */
	Potential sumOut(boolean[] sum_out_mask);

	/**
	 * Returns the dimensions of this potential.
	 * 
	 * @return unmodifiable List of Type
	 */
	List<Type> getDims();

	/**
	 * Returns the number associated with the given tuple of arguments.
	 * 
	 * @throws RuntimeException
	 *           if <code>indices</code> has the wrong length or one of the
	 *           elements of <code>indices</code> is outside the expected range.
	 */
	double getValue(List<?> arg_values);

	/**
	 * Sets the number associated with the given tuple of arguments.
	 * 
	 * @throws RuntimeException
	 *           if <code>indices</code> has the wrong length or one of the
	 *           elements of <code>indices</code> is outside the expected range.
	 */
	void setValue(List<?> arg_values, double value);

	void setValues(double[] values);

	/**
	 * Prints a human-readable representation of this potential to the given
	 * stream.
	 */
	void print(PrintStream out);

	void pow(double exp);

	void normalize();

	/**
	 * Returns true if this potential assigns weight zero to all tuples of
	 * arguments.
	 */
	public boolean isZero();

	public int size();

	Potential copy();

	/**
	 * Returns true if this potential has the same dimensions as the given
	 * potential, and each pair of corresponding entries differs by no more than
	 * Util.TOLERANCE.
	 */
	boolean withinTol(Potential other);
}
