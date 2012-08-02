package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import blog.common.Util;
import blog.distrib.Dirichlet;
import blog.distrib.Gamma;
import blog.model.BuiltInTypes;

/**
 * A test class to verify the Dirichlet distribution 
 * 
 * @author awong
 */
public class DirichletTest {
	
	private static final int NUM_DIMS = 5;
	
	private static final double ALL_EVI = 1.0;
	private static final double MAX_DIFF_DOUBLE = 1e-10;
	
	private Dirichlet distrib;
	private List<Double> vals;
	
	@Before
	public void createDirichlet() {
		distrib = new Dirichlet(NUM_DIMS, ALL_EVI);
		
		// Note: this test assumes a lack of evidence (i.e. \forall i, \alpha_i = 1)
		vals = new ArrayList<Double>();
		for (int i = 0; i < 5; i++) {
			vals.add(0.2);
		}
	}
	
	@Test
	public void testGetProb() {		
		double probDistrib = distrib.getProb(new ArrayList<Object>(), vals);
		double probTrue = Gamma.gamma(NUM_DIMS * ALL_EVI);
		for (double randVar: vals) {
			probTrue *= Math.pow(randVar, ALL_EVI - 1.0) / Gamma.gamma(ALL_EVI);
		}
		System.out.println(probDistrib);
		System.out.println(probTrue);
		
		assertEquals(probTrue, probDistrib, MAX_DIFF_DOUBLE);
	}
	
	@Test
	public void testGetLogProb() {
		double logProb = distrib.getLogProb(new ArrayList<Object>(), vals);
		double trueLog = Math.log(distrib.getProb(new ArrayList<Object>(), vals));
		
		assertEquals(logProb, trueLog, MAX_DIFF_DOUBLE);
	}
	
	@Test
	public void testSampleVal() {
		Util.initRandom(true);
		List<Double> samples = (List<Double>) distrib.sampleVal(new ArrayList<Object>(), BuiltInTypes.NULL);
		System.out.println(samples);
		
		double sum = 0.0;
		for (double paramVal: samples) {
			sum += paramVal;
		}
		assertEquals(sum, 1.0, MAX_DIFF_DOUBLE);
	}
	
	@Test
	public void testToString() {
		assertEquals(distrib.toString(), "blog.distrib.Dirichlet");
	}
}
