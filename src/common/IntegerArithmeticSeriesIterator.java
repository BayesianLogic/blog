package common;

/**
 * Implements an iterator going over an arithmetic series.
 * @author Rodrigo
 */
public class IntegerArithmeticSeriesIterator extends EZIterator {
    public IntegerArithmeticSeriesIterator(int initialElement, int finalElement, int step) {
	this.currentElement = initialElement;
	this.finalElement = finalElement;
	this.step = step;
	next = initialElement;
	onNext = true;
    }
    
    protected Object calculateNext() {
	currentElement += step;
	if (currentElement > finalElement)
	    return null;
	return currentElement;
    }
    
    int currentElement;
    int finalElement;
    int step;
}
