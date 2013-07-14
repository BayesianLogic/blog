package blog.distrib;

import java.util.*;
import blog.model.Type;

public class NextPosDistributionWrap extends AbstractCondProbDistrib {

	private Integer GridDimX;
	private Integer GridDimY;
	private double probStayInRegion;

	public NextPosDistributionWrap(List params) {
		probStayInRegion = 0.5;
		GridDimX = (Integer) params.get(0);
		GridDimY = (Integer) params.get(1);
		System.out.format("%d %d ", GridDimX, GridDimY);
	}

	private Integer getCoordinate(Integer r, Integer coordinate) {
		if (coordinate == 0) {
			return r % GridDimX;
		} else
			return r / GridDimX;
	}

	private Boolean ValidRegion(Integer r) {
		Integer x = getCoordinate(r, 0);
		Integer y = getCoordinate(r, 1);
		if (x < GridDimX && y < GridDimY && x >= 0 && y >= 0)
			return true;
		return false;
	}

	private Boolean ValidNbr(Integer r1, Integer r2) {
		Integer x1 = getCoordinate(r1, 0);
		Integer y1 = getCoordinate(r1, 1);
		Integer x2 = getCoordinate(r2, 0);
		Integer y2 = getCoordinate(r2, 1);

		if (ValidRegion(r1) && ValidRegion(r2) && Math.abs(x2 - x1) < 2
				&& Math.abs(y2 - y1) < 2) {
			return true;
		}
		return false;
	}

	private Integer getRegionNum(Integer x, Integer y) {
		return y * GridDimX + x;
	}

	private Integer getNbrRegion(Integer r, Integer deltax, Integer deltay) {
		Integer x1 = getCoordinate(r, 0);
		Integer y1 = getCoordinate(r, 1);

		Integer x2 = x1 + deltax;
		Integer y2 = y1 + deltay;

		if (x2 < GridDimX && y2 < GridDimY && x2 >= 0 && y2 >= 0)
			return getRegionNum(x2, y2);
		return -1;
	}

	private List<Integer> NbrsOf(Integer r1) {
		Integer x = getCoordinate(r1, 0);
		Integer y = getCoordinate(r1, 1);
		int r2;

		int count = 0;
		List nbrs = new ArrayList<Integer>();

		int[] deltas = { 0, 1, -1 };
		for (int deltax : deltas) {
			for (int deltay : deltas) {
				r2 = getNbrRegion(r1, deltax, deltay);
				if (r2 > -1) {
					nbrs.add(r2);
				}
			}
		}
		return nbrs;
	}

	private double transitionProb(Integer r1, Integer r2) {
		if (r1 == r2)
			return probStayInRegion;
		else if (ValidNbr(r1, r2))
			return (1.0 - probStayInRegion) / (NbrsOf(r1).size() - 1);
		return 0.0;
	}

	public double getProb(List args, Object value) {
		Integer r1 = (Integer) args.get(0);
		Integer r2 = (Integer) value;
		// System.out.format("start getting prob: %d -> %d\n", r1, r2);
		// System.out.format("done getting prob\n");

		// return 1.0;
		return transitionProb(r1, r2);
	}

	public Object sampleVal(List args, Type childType) {
		Integer r = (Integer) args.get(0);
		List<Integer> NbrList = NbrsOf(r);
		Integer NbrCount = NbrsOf(r).size();

		// System.out.format("start sampling\n");

		List<Double> probList = new ArrayList<Double>();
		probList.add(probStayInRegion);

		for (int i : NbrList) {
			probList.add(transitionProb(r, i));
		}

		Categorical distrib = new Categorical(probList);

		int sampledVal = distrib.sampleVal();
		// System.out.format("Done sampling: %d\n", sampledVal);

		if (sampledVal == 0)
			return r;
		else
			return NbrList.get(sampledVal - 1);
	}
}
