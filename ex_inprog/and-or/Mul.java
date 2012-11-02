

//package math;

import java.util.*;
import blog.*;
import common.Util;


public class Mul extends AbstractCondProbDistrib {
 
    public Mul(List params) {
    }


    private int getProd(List args){
        int val= ((Integer) args.get(0))*((Integer) args.get(1));
        return val;
    }

    public double getProb(List args, Object value) {
	int inputProd = (Integer) value;
	
	if (inputProd== getProd(args))
	    return 1;
	
	return 0;
    }

    public Object sampleVal(List args, Type childType) {
	return getProd(args);
    }    
}
