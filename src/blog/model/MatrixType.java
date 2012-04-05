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

package blog.model;

import java.util.*;

import blog.AbstractFunctionInterp;
import Jama.*;

/**
 * Represents a matrix or vector type.
 */
public class MatrixType extends Type {

	public MatrixType(String name, int rows, int columns, Type supertype) {
		super(name, supertype, true);
		if (rows <= 0)
			throw new IllegalArgumentException("Number of rows in a matrix "
					+ "must be positive; passed " + "value: " + rows);
		if (columns <= 0)
			throw new IllegalArgumentException("Number of columns in " + "a matrix "
					+ "must be positive; passed " + "value: " + columns);
		this.rows = rows;
		this.columns = columns;
	}

	public int getNumRows() {
		return rows;
	}

	public int getNumCols() {
		return columns;
	}

	/**
	 * Returns a non-random function that takes rows*cols real numbers as
	 * arguments, and returns a matrix of this type. The arguments are interpreted
	 * as entries in the matrix in row-major order.
	 */
	public NonRandomFunction getConstructor() {
		if (constructor == null) {
			List argTypes = Collections.nCopies(rows * columns, BuiltInTypes.REAL);
			constructor = new NonRandomFunction("Construct" + getName(), argTypes,
					this, new ConstructorInterp());
		}
		return constructor;
	}

	private class ConstructorInterp extends AbstractFunctionInterp {
		public Object getValue(List args) {
			double[][] entries = new double[rows][columns];
			int argIndex = 0;
			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < columns; ++j) {
					entries[i][j] = ((Number) args.get(argIndex++)).doubleValue();
				}
			}
			return new Matrix(entries, rows, columns);
		}
	}

	private int rows;
	private int columns;

	private NonRandomFunction constructor;
}
