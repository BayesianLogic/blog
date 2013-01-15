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
 * * Neither the name of the Massachusetts Institute of Technology 
 *   nor the names of its contributors may be used to endorse or 
 *   promote products derived from this software without specific  
 *   prior written permission.
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

import blog.common.Util;
import blog.model.Type;


/**
 * Abstract class for deterministic conditional probability distributions. These
 * are distributions such that for each tuple of CPD arguments, there is a child
 * value that has probability one.
 */
public abstract class DetCondProbDistrib extends AbstractCondProbDistrib {
	/**
	 * Returns the child value that has probability one for the given tuple of CPD
	 * arguments.
	 */
	public abstract Object getChildValue(List args);

	public double getProb(List args, Object childValue) {
		Object necessaryValue = getChildValue(args);
		boolean eq = necessaryValue.equals(childValue);
		/*
		 * if (Util.verbose() && !eq) {
		 * System.out.println("Deterministic CPD returning probability 0.");
		 * System.out.println("Value according to CPD: " + necessaryValue);
		 * System.out.println("Actual value: " + childValue); }
		 */
		return (eq ? 1 : 0);
	}

	public Object sampleVal(List args, Type childType) {
		return getChildValue(args);
	}
}
