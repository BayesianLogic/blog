package common;

import java.util.*;

/**
 * Iterator ranging over maps representing mappings between two given sets. Each
 * mapping involves one-to-one pairs, but may leave (no or all) elements on
 * either side unmapped.
 * 
 * @author Rodrigo
 */
public class IncompleteOneToOneMappingsIterator extends EZIterator {

	public IncompleteOneToOneMappingsIterator(LinkedList a, LinkedList b) {
		this.a = a;
		this.b = b;
		bOriginalSize = b.size();
		next = null;
		if (a.size() != 0) {
			aFirst = a.removeFirst();
		} else {
			next = new HashMap();
			onNext = true;
		}
	}

	protected Object calculateNext() {
		if (aFirst != null) { // if a was not empty
			if (aFirstElementNextMappeeIndex == -1) { // first go through cases with
																								// no mapping for aFirst
				if (subIterator == null) {
					subIterator = new IncompleteOneToOneMappingsIterator(a, b);
				}
				if (subIterator.hasNext())
					return subIterator.next();
				else {
					subIterator = null;
					aFirstElementNextMappeeIndex++;
				}
			}

			while (aFirstElementNextMappeeIndex != bOriginalSize) { // second, map
																															// aFirst to each
																															// element of b
				if (subIterator == null) {
					mappee = b.remove(aFirstElementNextMappeeIndex);
					subIterator = new IncompleteOneToOneMappingsIterator(a, b);
				}
				if (subIterator.hasNext()) {
					Map map = (Map) subIterator.next();
					map.put(aFirst, mappee);
					return map;
				} else {
					b.add(aFirstElementNextMappeeIndex, mappee);
					subIterator = null;
					aFirstElementNextMappeeIndex++;
				}
			}
			a.addFirst(aFirst);
		}

		return null;
	}

	protected LinkedList a;
	protected LinkedList b;
	protected int bOriginalSize;
	protected Object aFirst;
	protected Object mappee;
	protected int aFirstElementNextMappeeIndex = -1;
	protected Iterator subIterator;

	public static void main(String[] args) {
		Iterator it = new IncompleteOneToOneMappingsIterator(Util.list("x", "y",
				"z", "w"), Util.list(1, 2, 3, 4));
		int i = 0;
		while (it.hasNext()) {
			System.out.println(it.next());
			i++;
		}
		System.out.println("There were " + i + " mappings.");
	}
}
