package blog.distrib;

import java.util.List;

import blog.type.Timestep;

public class AddTime extends TemplateDistrib {
	private Timestep t;
	private Integer addition;

	public AddTime(List args) {
		super(args);
	}

	@Override
	int numParam() {
		return 2;
	}

	@Override
	void setParams(List params) {
		t = ((Timestep) params.get(0));
		addition = ((Number) params.get(1)).intValue();
	}

	@Override
	double getProb(Object value) {
		if (((Timestep) value).intValue() == t.intValue() + addition)
			return 1;
		else
			return 0;
	}

	@Override
	Object sampleVal() {
		return Timestep.at(t.intValue() + addition);
	}

}
