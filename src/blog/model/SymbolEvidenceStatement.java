/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import blog.bn.BasicVar;
import blog.bn.DerivedVar;
import blog.bn.RandFuncAppVar;
import blog.world.PartialWorld;

/**
 * Represents a symbol evidence statement, which is of the form <I>{type_name
 * var_name : cond_formula} = { symbol_list }</I>.
 */
public class SymbolEvidenceStatement {

	/**
	 * Creates a new SymbolEvidenceStatement saying that the objects satisfying
	 * the given implicit set specification are referred to by the given constant
	 * symbols.
	 * 
	 * @param setSpec
	 *          an implicit set specification
	 * @param symbols
	 *          a List of Strings representing Skolem constant names
	 */
	public SymbolEvidenceStatement(ImplicitSetSpec setSpec, List symbols) {
		this.setSpec = setSpec;

		List predecessors = new ArrayList();
		for (Iterator iter = symbols.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			SkolemConstant s = new SkolemConstant(name, setSpec, predecessors);
			skolemConstants.add(s);
			predecessors.add(s);
		}
	}

	/**
	 * Returns the implicit set specification that defines the set of objects
	 * labeled by the symbols.
	 */
	public ImplicitSetSpec getSetSpec() {
		return setSpec;
	}

	/**
	 * Returns a List of SkolemConstant objects representing the symbols
	 * introduced in this symbol evidence statement.
	 * 
	 * @return unmodifiable List of SkolemConstant objects
	 */
	public List<SkolemConstant> getSkolemConstants() {

		return Collections.unmodifiableList(skolemConstants);

	}

	/**
	 * Returns the cardinality variable whose value is observed in this evidence
	 * statement.
	 * 
	 * @throws IllegalStateException
	 *           if <code>compile</code> has not yet been called.
	 */
	public DerivedVar getObservedVar() {
		if (observedVar == null) {
			throw new IllegalStateException("Evidence statement has not "
					+ "been compiled yet.");
		}
		return observedVar;
	}

	/**
	 * Returns the observed value of the cardinality variable associated with this
	 * symbol evidence statement.
	 * 
	 * @throws IllegalStateException
	 *           if <code>compile</code> has not yet been called.
	 */
	public Integer getObservedValue() {
		if (observedValue == null) {
			throw new IllegalStateException("Evidence statement has not "
					+ "been compiled yet.");
		}
		return observedValue;
	}

	/**
	 * Returns true if this statement satisfies type and scope constraints. If
	 * there is a type or scope error, prints a message to standard error and
	 * returns false.
	 */
	public boolean checkTypesAndScope(Model model) {
		return setSpec.checkTypesAndScope(model, Collections.EMPTY_MAP);
	}

	/**
	 * Initializes the observed variable and value, and compiles the cardinality
	 * spec for the observed variable. Also compiles the dependency models for
	 * this statement's skolem constants.
	 */
	public int compile(LinkedHashSet callStack) {
		compiled = true;

		int errors = 0;

		CardinalitySpec cardSpec = new CardinalitySpec(setSpec);
		errors += cardSpec.compile(callStack);
		observedVar = new DerivedVar(cardSpec);
		observedValue = new Integer(skolemConstants.size());

		for (SkolemConstant c : skolemConstants) {
			errors += ((SkolemConstant) c).getDepModel().compile(callStack);
		}

		return errors;
	}

	/**
	 * Returns true if the given partial world is complete enough to determine
	 * whether this symbol evidence statement is true: that is, it determines what
	 * objects satisfy this statement's set specification, and it instantiates all
	 * the symbols.
	 */
	public boolean isDetermined(PartialWorld w) {
		if (!setSpec.isDetermined(w)) {
			return false;
		}

		for (Iterator iter = skolemConstants.iterator(); iter.hasNext();) {
			SkolemConstant sym = (SkolemConstant) iter.next();
			BasicVar symVar = new RandFuncAppVar(sym, Collections.EMPTY_LIST);
			if (w.getValue(symVar) == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if this symbol evidence statement is true in the given world:
	 * that is, its satisfying set has the right number of elements, and all of
	 * its symbols (if they are instantiated) refer to distinct elements of that
	 * set.
	 */
	public boolean isTrue(PartialWorld w) {
		Set satisfiers = setSpec.getSatisfyingSet(w);
		if (satisfiers.size() != skolemConstants.size()) {
		//	System.out.println("Symbol evidence statement should be "
		//			+ "satisfied by " + skolemConstants.size()
		//			+ " objects; actually satisfied by " + satisfiers.size() + ".");
			return false;
		}

		Set referents = new HashSet();
		for (Iterator iter = skolemConstants.iterator(); iter.hasNext();) {
			SkolemConstant sym = (SkolemConstant) iter.next();
			BasicVar symVar = new RandFuncAppVar(sym, Collections.EMPTY_LIST);
			Object referent = w.getValue(symVar);
			if (referent != null) {
				if (referents.contains(referent)) {
					System.out.println("Skolem constant " + sym
							+ " corefers with earlier constant.");
					return false;
				}
				if (!satisfiers.contains(referent)) {
					System.out.println("Skolem constant " + sym + " refers to "
							+ referent + " which doesn't satisfy set spec.");
					return false;
				}
				referents.add(referent);
			}
		}

		return true;
	}

	/**
	 * Returns an object whose toString method yields a description of the
	 * location where this statement occurred in an input file.
	 */
	public Object getLocation() {
		return setSpec.getLocation();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(setSpec);
		buf.append(" = {");
		for (int i = 0; i < skolemConstants.size(); ++i) {
			buf.append(skolemConstants.get(i));
			if (i + 1 < skolemConstants.size()) {
				buf.append(", ");
			}
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Replaces a term by another, returning a new SymbolEvidenceStatement if
	 * there is a replacement, or same if not. The Skolem constants are not
	 * considered for replacement. The new SymbolEvidenceStatement is compiled if
	 * this is compiled.
	 */
	public SymbolEvidenceStatement replace(Term t, ArgSpec another) {
		ImplicitSetSpec newSetSpec = (ImplicitSetSpec) setSpec.replace(t, another);
		if (newSetSpec != setSpec) {
			SymbolEvidenceStatement result = new SymbolEvidenceStatement(newSetSpec,
					skolemConstants);
			if (compiled)
				result.compile(new LinkedHashSet());
			return result;
		}
		return this;
	}

	private ImplicitSetSpec setSpec;
	private List<SkolemConstant> skolemConstants = new ArrayList<SkolemConstant>(); // of																													// SkolemConstant

	private boolean compiled = false;
	private DerivedVar observedVar;
	private Integer observedValue;
}
