package blog.distrib;

import java.util.*;

import blog.model.Type;

public class GridNbrOfWrap extends AbstractCondProbDistrib {

	private Integer GridDimX;
	private Integer GridDimY;
	private Integer r1;
	private Integer r2;
	private boolean fixedParam;

	public GridNbrOfWrap(List params) {
		if (params.isEmpty()) {
			fixedParam = true;
			return;
		} else if (params.size() == 4) {
			GridDimX = ((Number) params.get(0)).intValue();
			GridDimY = ((Number) params.get(1)).intValue();
			r1 = ((Number) params.get(2)).intValue();
			r2 = ((Number) params.get(3)).intValue();
		}
		System.out.format("Nbr: %d %d ", GridDimX, GridDimY);
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

		if (ValidRegion(r1)
				&& ValidRegion(r2)
				&& (Math.abs(x2 - x1) < 2 || Math.abs(x2 - x1) == (GridDimX - 1))
				&& Math.abs(y2 - y1) < 2) {
			return true;
		}
		return false;
	}

	private Integer getRegionNum(Integer x, Integer y) {
		return y * GridDimX + x;
	}

	public double getProb(boolean value) {
		Boolean inputValue = Boolean.valueOf(value);
		if (inputValue == ValidNbr(r1, r2))
			return 1;
		else
			return 0;

	}

	public Object sampleVal(List args, Type childType) {
		if (args.isEmpty() & fixedParam) {
			if (ValidNbr(r1, r2))
				return true;
			else
				return false;
		}
		else if (args.size() == 4 & !fixedParam) {
			setParams(args);
		}
		else {
			System.err
					.println("Error! screwed up parameters, very very screwed up");
			System.exit(1);
		}
		return null;
	}

	private void setParams (List args){
		GridDimX = ((Number) args.get(0)).intValue();
		GridDimY = ((Number) args.get(1)).intValue();
		r1 = ((Number) args.get(2)).intValue();
		r2 = ((Number) args.get(3)).intValue();
	}
	
	public double getProb(List args, Object value) {
		if (args.isEmpty() & fixedParam)
			return getProb(((Boolean) value).booleanValue());
		else if (args.size() == 4 & !fixedParam) {
			setParams(args);
			Boolean inputValue = (Boolean) value;

			System.err.println("");
			if (inputValue == ValidNbr(r1, r2))
				return 1;
			else
				return 0;
		} else {
			System.err
					.println("Error! screwed up parameters, very very screwed up");
			System.exit(1);
		}
		return -1;

	}
}
