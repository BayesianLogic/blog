package common;

import junit.framework.TestCase;
import java.util.*;

public class DistanceTest extends TestCase {

    public void testDistanceObjectObject() {
	Distance distance = new Distance();
	Object o1;
	Object o2;
	
	o1 = new Double(1);
	o2 = new Double(6);
	assertEquals(5.0, distance.distance(o1, o2));
	
	o1 = Util.set(Util.list(1.0, 1.0), Util.list(2.0, 2.0));
	o2 = Util.set(Util.list(3.0, 3.0), Util.list(4.0, 4.0));
	distance.approximateSetDistance = true;
	assertEquals(4*Math.sqrt(2.0), distance.distance(o1, o2), 0.000001);
	
	o1 = Util.set(Util.list(1.0, 1.0), Util.list(2.0, 2.0));
	o2 = Util.set(Util.list(3.0, 3.0), Util.list(4.0, 4.0));
	distance.approximateSetDistance = false;
	double distanceForBestMatching = 4*Math.sqrt(2.0);
	double distanceForWorstMatching = Math.sqrt(18) + Math.sqrt(2);
	double exactDistance = (distanceForBestMatching + distanceForWorstMatching)/2;
	assertEquals(exactDistance, distance.distance(o1, o2), 0.000001);
    }

    public void testDistanceDoubleDouble() {
	Distance distance = new Distance();
	assertEquals(5.0, distance.distance(new Double(1), new Double(6)));
    }

    public void testDistanceListList() {
	Distance distance = new Distance();
	assertEquals(Math.sqrt(2.0), distance.distance(Util.list(0.0,0.0), Util.list(1.0,1.0)), 0.000001);
	assertEquals(           0.0, distance.distance(Util.list(1.0,1.0), Util.list(1.0,1.0)), 0.000001);
    }

    public void testApproximateDistance() {
	Distance distance = new Distance();
	Set s1;
	Set s2;
	
	s1 = Util.set(Util.list(1.0, 1.0), Util.list(2.0, 2.0));
	s2 = Util.set(Util.list(2.0, 2.0), Util.list(3.0, 3.0));
	assertEquals(2*Math.sqrt(2.0), distance.approximateDistanceOfSets(s1, s2), 0.000001);
	
	s1 = Util.set(Util.list(1.0, 1.0), Util.list(2.0, 2.0));
	s2 = Util.set(Util.list(3.0, 3.0), Util.list(4.0, 4.0));
	assertEquals(4*Math.sqrt(2.0), distance.approximateDistanceOfSets(s1, s2), 0.000001);
    }

    public void testExactDistance() {
	Distance distance = new Distance();
	Set s1;
	Set s2;
	
	s1 = Util.set(Util.list(1.0, 1.0), Util.list(2.0, 2.0));
	s2 = Util.set(Util.list(3.0, 3.0), Util.list(4.0, 4.0));
	double distanceForBestMatching = 4*Math.sqrt(2.0);
	double distanceForWorstMatching = Math.sqrt(18) + Math.sqrt(2);
	double exactDistance = (distanceForBestMatching + distanceForWorstMatching)/2;
	assertEquals(exactDistance, distance.exactDistanceOfSets(s1, s2), 0.000001);
    }

    public void testDistanceObjectCollectionUnaryFunction() {
	Object o;
	Set set;
	UnaryFunction weightFunction;
	Distance distance = new Distance();
	
	o = Util.list(0.0,0.0,3.0);
	set = Util.set(Util.list(0.0,0.0,3.0), Util.list(0.0,0.0,1.0), Util.list(0.0,0.0,2.0));
	weightFunction = new UnaryFunction() { public Object evaluate(Object o) {
	    return 2.0; }
	};

	assertEquals(1.0, distance.distance(o, set, weightFunction), 0.0000001);    
    }

    public void testDistanceCollectionUnaryFunctionCollectionUnaryFunction() {
	Set s1;
	UnaryFunction weightFunction1;

	Set s2;
	UnaryFunction weightFunction2;

	Distance distance = new Distance();
	
	s1 = Util.set(Util.list(0.0,0.0,1.0), Util.list(0.0,0.0,2.0), Util.list(0.0,0.0,3.0));
	weightFunction1 = new UnaryFunction() { public Object evaluate(Object o) {
	    return 1.0; }
	};
	
	s2 = Util.set(Util.list(0.0,0.0,3.0), Util.list(0.0,0.0,1.0), Util.list(0.0,0.0,2.0));
	weightFunction2 = new UnaryFunction() { public Object evaluate(Object o) {
	    return 2.0; }
	};

	assertEquals((1.0 + 1.0 + 2.0/3)/3, distance.distance(s1, weightFunction1, s2, weightFunction2), 0.0000001);    
    }

}
