/*
 * Copyright (c) 2005, 2006, Regents of the University of California
 *
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
import blog.common.Util;
import blog.model.BuiltInFunctions;
import blog.model.BuiltInTypes;
import blog.model.EnumeratedObject;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.ListSpec;
import blog.model.Term;
import blog.model.Type;
import blog.model.MapSpec;
import blog.model.ArgSpec;
import blog.model.SymbolTerm;
import blog.model.Clause;

import java.util.*;
import Jama.Matrix;

/**
 * @author rbharath
 *
 * CPD which maps objects to distributions. The CPD can have any number 
 * of arguments, which are the parents. The Distributions must all be 
 * over the desired child type and must not accept any arguments (that is,
 * they cannot be conditionally defined). The child type can also be 
 * Integer or NaturalNum, in which case the distribution is over some 
 * prefix of the natural numbers.
 * 
 * <p>
 * Internally, this cpd maintains a map which sends ArgSpec Objects to 
 * Distributions. Currently, ArgSpec objects represent single parent values. 
 * Once a Tuple class is added, ArgSpecs may represent more parent values
 * The ordering of the parents in the arguments must match the order in which
 * the parents were specified in the TabularCPD declaration.
 * 
 * <p>
 * Currently, all values of the parents must be specified in the map. 
 * This should change in the future.
 * 
 * <p>
 * When one or more the parent values are <code>Model.NULL</code>, the child
 * distribution is also concentrated on <code>Model.NULL</code>  
 */
public class TabularCPD extends AbstractCondProbDistrib {
	/**
	 * Creates a new tabular CPD from the given parameter list.
	 * Params should be of length one, containing only one element of MapSpec.
	 * This map will be stored and used internally.
	 */
	public TabularCPD(List params) {
		if (params.isEmpty()) {
			throw new IllegalArgumentException("Tabular CPD must have "
					+ "at least one distribution.");
		}

        if (params.size() == 1) {
            Object param = params.get(0);
            if (param instanceof Map) {
                this.map = (Map<ArgSpec, Object>) param;
            }
        } else {
            throw new IllegalArgumentException("TabularCPD may only be "
                    + "specified with a map");
        }

	}

	public double getProb(List args, Object value) {
		if (args.contains(Model.NULL)) {
			return ((value == Model.NULL) ? 1 : 0);
		}

        ArgSpec parents = getArgSpec(args);
        Clause c = (Clause) this.map.get(parents);
        CondProbDistrib distrib = (CondProbDistrib) c.getCPD();
        if (distrib == null) {
            throw new IllegalArgumentException("TabularCPD args don't "
                + "specify a distribution");
        }

        return distrib.getProb(new LinkedList(), value); 
	}

	public Object sampleVal(List args, Type childType) {
		if (args.contains(Model.NULL)) {
			return Model.NULL;
		}

        ArgSpec parents = getArgSpec(args);
        Clause c = (Clause) this.map.get(parents);
        CondProbDistrib distrib = c.getCPD();
        if (distrib == null) {
            throw new IllegalArgumentException("TabularCPD args don't "
                + "specify a distribution");
        }

        return distrib.sampleVal(new LinkedList(), childType); 
	}

    // TODO: Implement this correctly
	public String toString() {
		StringBuffer buffer = new StringBuffer("[");
		for (ArgSpec as : this.map.keySet()) {
			buffer.append(as);
		}
		buffer.append("]");
		return buffer.toString();
	}

    private ArgSpec getArgSpec(List params) {
        if (params.size() == 1) {
            Object o = params.get(0);
            if (o instanceof EnumeratedObject) {
                EnumeratedObject e = (EnumeratedObject) o;
                return new SymbolTerm(e.getName());
            }
            // TODO: add more types as they are required by getArgSpec,
            //       or find a better way to handle built-in types
            else if (o instanceof BooleanDistrib) {
            	BooleanDistrib b = (BooleanDistrib) o;
            	return new FuncAppTerm(BuiltInFunctions.getLiteral(
        								String.valueOf(b), BuiltInTypes.BOOLEAN, b));
            }
            else if (o instanceof List) {
            	List<Object> arg = (List) o;
            	List<ArgSpec> terms = new ArrayList<ArgSpec>();
            	for (Object obj: arg) {
            		List<Object> wrapper = new LinkedList<Object>();
            		wrapper.add(obj);
            		terms.add(this.getArgSpec(wrapper));
            	}
            	
            	ListSpec multi = new ListSpec(terms);
            	return multi;
            }
            else {
                throw new IllegalArgumentException("TabularCPD: parameters to "
                    + "sampleVal must be EnumeratedObjects, built-in types, or lists");
            }
        } else {
            throw new IllegalArgumentException("Arguments to TabularCPD must "
                + "be instance of ArgSpec");
        }
    }

    Map<ArgSpec, Object> map;
}
