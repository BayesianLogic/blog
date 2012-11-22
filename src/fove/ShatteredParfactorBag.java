package fove;

import java.util.*;

import blog.common.HashMultiMap;
import blog.common.MultiMap;
import blog.model.ArgSpec;
import blog.model.FuncAppTerm;
import blog.model.FunctionSignature;
import blog.model.Term;

/**
 * Utility class for maintaining a shattered set of parfactors.
 * 
 * Acts mostly like a set, except that it modifies itself under addition to
 * maintain the condition that all parfactors contained in the instance are
 * shattered with respect to each other.
 */
public class ShatteredParfactorBag {

	private static final boolean DEBUG = false;

	/**
	 * Create a new ShatteredParfactorBag, with the same probability distribution
	 * as the argument set.
	 */
	public ShatteredParfactorBag(Set<Parfactor> parfactors) {
		shatteredParfactors = new LinkedHashSet<Parfactor>();
		signatureToPF = new HashMultiMap(true);
		for (Parfactor pf : parfactors)
			add(pf);
	}

	/**
	 * Add an individual parfactor to the shattered set.
	 */
	public void add(Parfactor pf) {
		Stack<Parfactor> s = new Stack<Parfactor>();
		s.push(pf);
		addAll(s);
	}

	/**
	 * Add a collection of parfactors to the shattered set.
	 */
	public void addAll(Collection<Parfactor> pfs) {
		Stack<Parfactor> s = new Stack<Parfactor>();
		for (Parfactor pf : pfs)
			s.push(pf);
		addAll(s);
	}

	// does the work
	private void addAll(Stack<Parfactor> stack) {
		if (DEBUG) {
			System.out.println("----------------");
			System.out.println("------START-----");
			System.out.println("----------------");
		}

		/*
		 * postcondition:
		 * 
		 * - shatteredParfactors is shattered; grounds to the union of its current
		 * grounding with the grounding of all parfactors in queue.
		 * 
		 * - signatureToPF accurately reflects the function signatures in
		 * shatteredParfactors
		 */
		while (!stack.empty()) {
			if (DEBUG) {
				System.out.println("Shattered parfactors: ");
				for (Parfactor p : shatteredParfactors)
					System.out.println("\t" + p);
			}

			Parfactor dirty = stack.pop();
			List<? extends Term> terms = dirty.dimTerms();

			if (DEBUG)
				System.out.println("Got dirty parfactor:\n\t" + dirty);

			boolean foundSplit = false;
			outer_for: for (int i = 0; i < terms.size(); i++) {
				// terms that might have partial overlap with this term,
				// but are already shattered w.r.t. each other. it's
				// important to make a copy, because we'll modify the
				// multimap-backed set when we call addSafe below.
				Set<Parfactor.TermPtr> cleanTerms = termsWithSameSignature(terms.get(i));

				if (cleanTerms.isEmpty())
					continue;

				Set<Parfactor> residuals = new LinkedHashSet<Parfactor>();

				for (Parfactor.TermPtr cleanPtr : cleanTerms) {
					Parfactor cpf = cleanPtr.parfactor();
					int cIndex = cleanPtr.index();
					if (!shatteredParfactors.contains(cpf)) {
						continue; // already removed due to earlier term
					}

					residuals.clear();

					if (cpf.shatter(dirty, cIndex, i, residuals)) {
						if (DEBUG) {
							System.out.println("Split:\t" + cpf + "\n\t at " + cIndex
									+ "\n  and:\t" + dirty + "\n\tat " + i);
							System.out.println("Residuals: ");
							for (Parfactor p : residuals) {
								System.out.println("\t" + p);
							}
						}
						remove(cpf);
						for (Parfactor p : residuals)
							stack.push(p);

						// NB: breaking here, while correct, throws away
						// the information that we shattered `dirty` with
						// all of `cleanResiduals` at `cleanPtr`; we'll
						// check this again a few times, so there's room
						// for improvement here.
						foundSplit = true;
						if (DEBUG) {
							if (!isShattered())
								throw new IllegalStateException();
						}

						break outer_for;
					}
				}
			}

			if (!foundSplit) {
				addSafe(dirty);
			}
			if (DEBUG)
				System.out.println("\n\n");
		}

		if (DEBUG) {
			if (!isShattered())
				throw new IllegalStateException();
			System.out.println("----------------");
			System.out.println("-------DONE-----");
			System.out.println("----------------");
			System.out.println();
			System.out.println();
		}
	}

	/**
	 * Returns an unmodifiable collection view of the parfactors in the bag.
	 */
	public Collection<Parfactor> parfactors() {
		return Collections.unmodifiableSet(shatteredParfactors);
	}

	/**
	 * Ensures that all parfactors are split with respect to the argument
	 * collection of (ground) query terms.
	 */
	public void splitOnQueryTerms(Collection<? extends Term> queryTerms) {
		for (Term query : queryTerms) {
			boolean foundSplit = true;
			while (foundSplit) {
				foundSplit = false;
				Set<Parfactor.TermPtr> cleanTerms = termsWithSameSignature(query);
				Set<Parfactor> residuals = new LinkedHashSet<Parfactor>();
				for (Parfactor.TermPtr tPtr : cleanTerms) {
					Parfactor pf = tPtr.parfactor();
					if (!shatteredParfactors.contains(pf)) {
						continue; // already removed due to earlier term
					}

					residuals.clear();
					if (pf.shatter(tPtr.index(), query, residuals)) {
						foundSplit = true;
						remove(pf);
						for (Parfactor res : residuals)
							add(res);
					}
				}
			}
		}
		if (DEBUG) {
			if (!isShattered(queryTerms))
				throw new IllegalStateException();
		}
	}

	// add a parfactor that's guaranteed shattered with respect to
	// shatteredParfactors
	private void addSafe(Parfactor p) {
		if (DEBUG) {
			System.out.println("Adding: " + p);
		}

		shatteredParfactors.add(p);
		indexFunctionSignatures(p);
	}

	// asserts that the given parfactor is in `shatteredParfactors`,
	// removes it from that set, and removes TermPtr entries related to it
	// from `signatureToPF`
	private void remove(Parfactor pf) {
		if (DEBUG)
			System.out.println("Removing:\n\t" + pf);

		if (!shatteredParfactors.remove(pf)) {
			throw new IllegalArgumentException(
					"Attempting to remove unknown parfactor");
		}

		for (int i = 0; i < pf.numDimTerms(); i++) {
			Parfactor.TermPtr tptr = pf.termPtr(i);
			FunctionSignature sig = termSignature(tptr.term());
			if (!signatureToPF.remove(sig, tptr)) {
				throw new IllegalStateException(
						"had parfactor in shatteredParfactors, but term" + " "
								+ tptr.term() + " was not properly indexed."
								+ " multimap had: " + signatureToPF.get(sig));
			}
		}
	}

	// adds signatures from the terms in pf to `signatureToPF`
	private void indexFunctionSignatures(Parfactor pf) {
		// this lets us build a multimap from function signatures in atoms
		// to term pointers into the parfactors containing them, e.g.,
		// given parfactors
		//
		// pf1 = (L, C, (a1, a2), phi(a1,a2))
		// pf2 = (L, C, (a1, a3), phi(a1,a3))
		//
		// we get the multimap
		//
		// { a1: {pf1[0], pf2[0]}, a2: {pf1[1]}, a3: {pf2[1]} }

		if (DEBUG)
			System.out.println("Indexing:\n\t" + pf);

		List<? extends Term> atoms = pf.dimTerms();
		for (int i = 0; i < atoms.size(); i++) {
			Term t = atoms.get(i);
			Parfactor.TermPtr tPtr = pf.termPtr(i);
			FunctionSignature sig = termSignature(t);
			if (DEBUG)
				System.out.println("\t" + "added sig " + sig + " at " + i);
			signatureToPF.add(sig, tPtr);
		}
		if (DEBUG)
			System.out.println("... done indexing.");
	}

	private Set<Parfactor.TermPtr> termsWithSameSignature(Term t) {
		FunctionSignature signature = termSignature(t);
		return new LinkedHashSet<Parfactor.TermPtr>(
				(Set<Parfactor.TermPtr>) signatureToPF.get(signature));
	}

	// get a signature for the relevant function application. for regular
	// FuncAppTerm this is just the signature of the application's
	// function; for CountingTerm this is the signature taken from the
	// FuncAppTerm that the CountingTerm counts over. any other types are
	// errors.
	private FunctionSignature termSignature(Term t) {
		FunctionSignature termSig = null;
		if (t instanceof FuncAppTerm) {
			termSig = ((FuncAppTerm) t).getFunction().getSig();
		} else if (t instanceof CountingTerm) {
			FuncAppTerm fat = ((CountingTerm) t).singleSubTerm();
			termSig = fat.getFunction().getSig();
		} else {
			throw new IllegalArgumentException(
					"can only handle CountingTerms and FuncAppTerms," + " but got: "
							+ t.getClass().getName());
		}
		return termSig;
	}

	// this should only be used for debugging, to detect errors, as it'll
	// destructively and **lossily** modify shatteredParfactors.
	private boolean isShattered() {
		return isShattered(Collections.<Term> emptyList());
	}

	// this should only be used for debugging, to detect errors, as it'll
	// destructively and **lossily** modify shatteredParfactors.
	private boolean isShattered(Collection<? extends Term> queryTerms) {
		if (DEBUG)
			System.out.print("Checking if shattered on " + queryTerms + "...");

		// an expensive check to make sure the parfactors are shattered
		List<Parfactor> pfList = new ArrayList<Parfactor>(shatteredParfactors);

		// find a pair of atoms that overlap
		// first two loops over parfactors
		for (int i = 0; i < pfList.size(); i++) {
			Parfactor p_i = pfList.get(i);
			for (int j = i; j < pfList.size(); j++) {
				Parfactor p_j = pfList.get(j);

				// inner two loops are over atoms in parfactors
				for (int k = 0; k < p_i.numDimTerms(); k++) {
					for (int l = 0; l < p_j.numDimTerms(); l++) {
						if (p_i.shatter(p_j, k, l, pfList)) {
							System.out.println("\nOOPS:\t" + p_i + "\n" + "  at: " + k
									+ "\n and:\t" + p_j + "at " + l + "\n\tARE NOT SHATTERED.");
							return false;
						}
					}
				}
			}

			// ensure we're split on query terms
			for (Term queryTerm : queryTerms) {
				for (int k = 0; k < p_i.numDimTerms(); k++) {
					if (p_i.shatter(k, queryTerm, pfList)) {
						return false;
					}
				}
			}
		}

		if (DEBUG)
			System.out.println(" shattered!");
		return true;
	}

	// the shattered set of parfactors we maintain
	private Set<Parfactor> shatteredParfactors;
	// see `indexFunctionSignatures`
	private MultiMap signatureToPF;
}
