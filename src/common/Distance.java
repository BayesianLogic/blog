package common;

import java.util.*;

public class Distance {

	public double distance(Object o1, Object o2) {
		if (o1 instanceof Set || o1 instanceof Multiset)
			return distanceOfSets((Collection) o1, (Collection) o2);
		if (o1 instanceof List)
			return distance((List) o1, (List) o2);
		else if (o1 instanceof Double)
			return distance((Double) o1, (Double) o2);
		return 0;
	}

	public double distance(Double o1, Double o2) {
		return Math.abs(o1.doubleValue() - o2.doubleValue());
	}

	public double distance(List l1, List l2) {
		if (l1.size() != l2.size())
			Util.fatalError("Cannot calculate distance of lists of different sizes.");

		double sumOfDistanceSquares = 0;
		Iterator it1 = l1.iterator();
		Iterator it2 = l2.iterator();
		while (it1.hasNext()) {
			Object o1 = it1.next();
			Object o2 = it2.next();
			double d = distance(o1, o2);
			sumOfDistanceSquares += d * d;
		}
		return Math.sqrt(sumOfDistanceSquares);
	}

	public double distanceOfSets(Collection s1, Collection s2) {
		if (approximateSetDistance)
			return approximateDistanceOfSets(s1, s2);
		return exactDistanceOfSets(s1, s2);
	}

	public double approximateDistanceOfSets(Collection s1, Collection s2) {
		if (s1.size() != s2.size())
			return noMatchDistance;

		double[][] distanceMatrix = makeDistanceMatrix(s1, s2);

		BipartiteMatcher matcher = new BipartiteMatcher(s1.size());
		for (int i = 0; i != s1.size(); i++) {
			for (int j = 0; j != s2.size(); j++) {
				matcher.setWeight(i, j, -distanceMatrix[i][j]);
			}
		}

		int[] bestMatching = matcher.getMatching();

		double sum = getDistanceOfMatching(distanceMatrix, bestMatching);

		return sum;
	}

	private double getDistanceOfMatching(double[][] distanceMatrix, int[] matching) {
		double sum = 0;
		for (int i = 0; i != matching.length; i++) {
			sum += distanceMatrix[i][matching[i]];
		}
		return sum;
	}

	/**
	 * Makes a distance matrix (row and columns refer to the order of the given
	 * set's iterators.
	 */
	private double[][] makeDistanceMatrix(Collection s1, Collection s2) {
		double[][] distanceMatrix = new double[s1.size()][s2.size()];
		Iterator it1 = s1.iterator();
		for (int i = 0; it1.hasNext(); i++) {
			Object o1 = it1.next();
			Iterator it2 = s2.iterator();
			for (int j = 0; it2.hasNext(); j++) {
				Object o2 = it2.next();
				double distanceValue = distance(o1, o2);
				distanceMatrix[i][j] = distanceValue;
			}
		}
		return distanceMatrix;
	}

	/**
	 * Calculates the exact distance from two sets, which is the average of the
	 * distances over all matchings.
	 */
	public double exactDistanceOfSets(Collection s1, Collection s2) {
		if (s1.size() != s2.size())
			return noMatchDistance;

		double[][] distance = makeDistanceMatrix(s1, s2);

		double sum = 0;
		PermutationIterator permutationIt = new PermutationIterator(s1.size(),
				s2.size());
		while (permutationIt.hasNext()) {
			int[] permutation = (int[]) permutationIt.next();
			sum += getDistanceOfMatching(distance, permutation);
		}

		return sum / Util.factorial(s1.size());
	}

	/**
	 * Returns the weighted average of a given object's distances to a weighted
	 * set.
	 */
	public double distance(Object o, Collection c, UnaryFunction weight) {
		double sum = 0;
		double sumOfWeights = 0;
		for (Object oPrime : c) {
			double d = distance(o, oPrime);
			double w = ((Double) weight.evaluate(oPrime)).doubleValue();
			sum += w * d;
			sumOfWeights += w;
		}
		return sum / sumOfWeights;
	}

	/**
	 * Returns the weighted average of a collection's objects' weighted average
	 * distances to another set.
	 */
	public double distance(Collection c1, UnaryFunction weight1, Collection c2,
			UnaryFunction weight2) {
		double sum = 0;
		double sumOfWeights = 0;
		for (Object o1 : c1) {
			double d = distance(o1, c2, weight2);
			double w1 = ((Double) weight1.evaluate(o1)).doubleValue();
			sum += w1 * d;
			sumOfWeights += w1;
		}
		return sum / sumOfWeights;
	}

	/** Determines whether the distance between sets is approximate or exact. */
	public boolean approximateSetDistance = true;

	/** The value to be used as distance between two incomparable elements. */
	public double noMatchDistance = Double.POSITIVE_INFINITY;
}
