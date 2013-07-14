package blog.distrib;

import java.util.List;

import blog.type.Timestep;

public class MultTime extends TemplateDistrib {
	private Integer multiple;
	private Integer t;

	public MultTime(List args) {
		super(args);
	}

	@Override
	int numParam() {
		return 2;
	}

	@Override
	void setParams(List params) {
		multiple = ((Number) params.get(0)).intValue();
		t = ((Number) params.get(1)).intValue();
	}

	@Override
	double getProb(Object value) {
		if (((Timestep) value).intValue() == multiple * t)
			return 1;
		else
			return 0;
	}

	@Override
	Object sampleVal() {
		return Timestep.at(multiple * t);
	}

}
