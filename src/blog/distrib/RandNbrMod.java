package blog.distrib;

import java.util.List;

import blog.common.Util;

public class RandNbrMod extends TemplateDistrib{

	private Integer pos;
	private Integer max;
	public RandNbrMod(List params) {
		super(params);
	}

	@Override
	int numParam() {
		return 2;
	}

	@Override
	void setParams(List params) {
		pos = (Integer) params.get(0);
		max = (Integer) params.get(1);
		
	}

	private Boolean Next(Integer r1, Integer r2, Integer diff){
		return (r2 == modAdd(r1, diff));
	}
        
	private Boolean Prev(Integer r1, Integer r2, Integer diff){
		return (r2 == modSub(r1,diff));
	}
        
	private Integer modAdd(Integer r1, Integer diff){
                                return (r1+diff )% max;
	}
	private Integer modSub(Integer r1, Integer diff){
                                return (r1+max - diff) % max;
	}
	
	
	private double transitionProb(Integer r1, Integer r2) {
		if (r1 == r2)
			return 0.4;
		else if (Next(r1, r2, 1) || Prev(r1, r2, 1))
			return 0.2;
                                else if (Next(r1, r2, 2) || Prev(r1, r2, 2))
                                                return 0.1;
		else
			return 0;
	}


	@Override
	double getProb(Object value) {
		Integer r1 = (Integer) pos;
		Integer r2 = (Integer) value;
		//System.err.println("returning prob correctly");
		return transitionProb(r1, r2);
	}

	@Override
	Object sampleVal() {
		double[] probList = new double[max];

		for (int i =0;i<max;i++) {
			probList[i]=(transitionProb(pos, i));
		}
		//System.err.println("returning val correctly");
		return Integer.valueOf((Util.sampleWithProbs(probList)));	
	}

}
