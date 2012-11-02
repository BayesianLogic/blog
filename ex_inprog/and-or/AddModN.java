

//package math;

import java.util.*;
import blog.*;
import common.Util;


public class AddModN extends AbstractCondProbDistrib {
 
    public AddModN(List params) {
    }


    private int getModSum(List args){
        int modSum;
        int trueSum=0;
        int N = (Integer) args.get(args.size()-1);

        for(int i=0;i<args.size()-1;i++){
            trueSum += (Integer) args.get(i);
        }

	modSum = trueSum % N;
	return modSum;
    }

    public double getProb(List args, Object value) {
	int inputSum = (Integer) value;
	
	if (inputSum == getModSum(args))
	    return 1;
	
	return 0;
    }

    public Object sampleVal(List args, Type childType) {
	return getModSum(args);
    }    
}
