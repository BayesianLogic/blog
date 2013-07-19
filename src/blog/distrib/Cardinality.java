package blog.distrib;

import java.util.List;

import blog.objgen.POPAppBasedSet;

public class Cardinality extends TemplateDistrib{

	private POPAppBasedSet set;
	public Cardinality(List params) {
		super(params);
	}

	@Override
	int numParam() {
		return 1;
	}

	@Override
	void setParams(List params) {
		set = (POPAppBasedSet) params.get(0);
		
	}

	@Override
	double getProb(Object value) {
		if (((Number) value).intValue()==set.size())
			return 1;
		else
			return 0;
	}

	@Override
	Object sampleVal() {
		return set.size();
	}

}
