package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import blog.common.Util;
import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.distrib.Laplace;
import blog.model.BuiltInTypes;

/**
 * A test class to verify the Laplace distribution 
 * 
 * @author awong
 */
public class LaplaceTest {
	
	private static final double MEAN = 0.0;
	
	private static final double DIVERSITY = 1.0;
	//private static final double MAX_DIFF_DOUBLE = 1e-10;
	
	private Laplace distrib;
	private static final int NUM_OF_SAMPLES = 1000;
	
	@Before
	public void createLaplace() {
		distrib = new Laplace(MEAN, DIVERSITY);

	}
	
	
	@Test
	public void testSampleVal() {
		Util.initRandom(true);
		double sample, sample_mean = 0, sample_variance = 0;
		System.out.println("Number of samples: " + NUM_OF_SAMPLES);
		System.out.print("Samples: ");
		for(int i = 0; i < NUM_OF_SAMPLES; i++) {
			sample = distrib.sampleVal();
			System.out.print(sample + "\t");
			sample_mean += sample;
			sample_variance += sample * sample;
		}
		sample_mean /= NUM_OF_SAMPLES;
		sample_variance = (sample_variance - NUM_OF_SAMPLES * sample_mean * sample_mean) / (NUM_OF_SAMPLES - 1);
		System.out.println();
		System.out.println("Sample mean: " + sample_mean + ", true mean: " + distrib.getMean());
		System.out.println("Sample variance: " + sample_variance + ", true variance: " + distrib.getVariance());
	}
	
	@Test
	public void testToString() {
		System.out.println(distrib.toString());
	}
}
