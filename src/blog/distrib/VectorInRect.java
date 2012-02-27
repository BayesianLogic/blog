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

import java.util.*;
import Jama.Matrix;

/**
 * Function that takes a column vector as an argument and returns true  
 * if the vector is in a certain rectangle.  The rectangle is specified by 
 * the parameters to the constructor.  These parameters should be a list of 
 * real numbers (min_1, max_1, min_2, max_2, ..., min_d, max_d).  Then 
 * the function returns true on (x_1, x_2, ..., x_d) if 
 * min_1 &le; x_1 &le; max_1, ..., and min_d &le; x_d &le; max_d.
 */
public class VectorInRect extends blog.AbstractFunctionInterp {
    /**
     * Creates a new VectorInRect function interpretation with min and 
     * max values for each dimension specified by the given parameter 
     * list.
     *
     * @param params list of Number objects
     */
    public VectorInRect(List params) {
	if (params.size() % 2 != 0) {
	    throw new IllegalArgumentException
		("VectorInRect expects an even number of parameters, "
		 + "forming min-max pairs.");
	}

	int d = params.size() / 2;
	mins = new double[d];
	maxes = new double[d];

	for (int i = 0; i < d; ++i) {
	    mins[i] = ((Number) params.get(2*i)).doubleValue();
	    maxes[i] = ((Number) params.get((2*i) + 1)).doubleValue();
	}
    }

    /**
     * Returns Boolean.TRUE if the first element of args is a column vector 
     * (i.e., a Matrix with one column) in the specified rectangle; otherwise 
     * returns Boolean.FALSE.  
     */
    public Object getValue(List args) {
	if (args.size() != 1) {
	    throw new IllegalArgumentException
		("VectorInRect expects exactly one argument.");
	}

	Matrix v = (Matrix) args.get(0);
	if ((v.getRowDimension() != mins.length)
	        || (v.getColumnDimension() != 1)) {
	    throw new IllegalArgumentException
		("Argument to VectorInRect should be " + mins.length
		 + "x1 vector, not " + v.getRowDimension() + "x"
		 + v.getColumnDimension() + "matrix.");
	}

	for (int i = 0; i < mins.length; ++i) {
	    if ((v.get(i, 0) < mins[i]) || (v.get(i, 0) > maxes[i])) {
		return Boolean.FALSE;
	    }
	}
	return Boolean.TRUE;
    }
    
    private double[] mins;
    private double[] maxes;
} 
