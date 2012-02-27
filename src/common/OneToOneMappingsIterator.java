package common;

import java.util.*;

/**
 * Iterator ranging over maps representing one to one mappings between two given sets.
 * @author Rodrigo
 */
public class OneToOneMappingsIterator extends EZIterator {

    // TODO: this may not be a very efficient implementation,
    // having been adapted from a more complex mapping iterator.
    
    public OneToOneMappingsIterator(LinkedList a, LinkedList b) {
	this.a = a;
	this.b = b;
	if (a.size() == b.size()) {
	    bOriginalSize = b.size();
	    if (a.size() != 0) {
		aFirst = a.removeFirst();
	    }
	    else {
		next = new HashMap();
		onNext = true;
	    }
	}
	else {
	    next = null;
	    onNext = true;
	}
    }
    
    protected Object calculateNext() {
	if (aFirst != null) { // if a was not empty
	    while (aFirstElementNextMappeeIndex != bOriginalSize) { // second, map aFirst to each element of b
		if (subIterator == null) {
		    mappee = b.remove(aFirstElementNextMappeeIndex);
		    subIterator = new OneToOneMappingsIterator(a, b);
		}
		if (subIterator.hasNext()) {
		    Map map = (Map) subIterator.next();
		    map.put(aFirst, mappee);
		    return map;
		}
		else {
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
    protected int aFirstElementNextMappeeIndex = 0;
    protected Iterator subIterator;
    
    public static void main(String[] args) {
	Iterator it = new OneToOneMappingsIterator(
//		Util.list("A", "B"),
//		Util.list(1)
		Util.list("w", "x", "y", "z"),
		Util.list(1, 2, 3, 4)
		);
	int i = 0;
	while (it.hasNext()) {
	    System.out.println(it.next());
	    i++;
	}
	System.out.println("There were " + i + " mappings.");
    }
}
