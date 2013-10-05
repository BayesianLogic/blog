package blog.distrib;

import java.util.HashMap;
import java.util.List;

import blog.common.Util;

public class NoisyNum extends TemplateDistrib{

	private Integer num;
	private HashMap<Integer, Double> valtoprob = new HashMap();
	public NoisyNum(List params) {
		super(params);
	}

	@Override
	int numParam() {
		return 1;
	}

	@Override
	void setParams(List params) {
		num = (Integer) params.get(0);
		Integer m2 = Math.max(0, num-2);
		Integer m1 = Math.max(0, num-1);
		Integer m0 = num;
		Integer a1 = Math.max(0, num+1);
		Integer a2 = Math.max(0, num+2);
		valtoprob.put(m2, 0.0);
		valtoprob.put(m1, 0.0);
		valtoprob.put(m0, 0.0);
		valtoprob.put(a1, 0.0);
		valtoprob.put(a2, 0.0);
		valtoprob.put(m2, valtoprob.get(m2)+0.1);
		valtoprob.put(m1, valtoprob.get(m1)+0.2);
		valtoprob.put(m0, valtoprob.get(m0)+0.4);
		valtoprob.put(a1, valtoprob.get(a1)+0.2);
		valtoprob.put(a2, valtoprob.get(a2)+0.1);
		
	}


	@Override
	double getProb(Object value) {
		Integer val = (Integer) value;
		if (valtoprob.containsKey(val))
			return valtoprob.get(val);
		else
			return 0.0;
	}

	@Override
	Object sampleVal() {
		int m2 = Math.max(0, num-2);
		int m1 = Math.max(0, num-1);
		int m0 = num;
		int a1 = Math.max(0, num+1);
		int a2 = Math.max(0, num+2);
		int[] valList = {m2, m1, m0, a1, a2};
		double[] probList = new double[5];

		probList[0]=0.1;
		probList[1]=0.2;
		probList[2]=0.4;
		probList[3]=0.2;
		probList[4]=0.1;
		return Integer.valueOf(valList[(Util.sampleWithProbs(probList))]);	
	}

}
