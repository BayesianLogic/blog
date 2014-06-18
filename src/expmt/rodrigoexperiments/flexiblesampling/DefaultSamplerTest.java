package expmt.rodrigoexperiments.flexiblesampling;

import expmt.rodrigoexperiments.flexiblesampling.Sampler.Sample;
import junit.framework.TestCase;

import blog.BLOGUtil;
import blog.common.Util;
import blog.model.Model;
import blog.world.DefaultPartialWorld;

public class DefaultSamplerTest extends TestCase {

	public void setUp() {
		Util.initRandom(true);
	}

	public void testBasic() {

		Model model = Model
				.fromString("random Boolean X ~ Bernoulli(0.7);"
						+ "random Boolean Y   if X then ~ Bernoulli(0.3) else ~ Bernoulli(0.9);");

		SamplerFactory factory = new DefaultSamplerFactory();

		Sampler sampler = factory.make(BLOGUtil.parseVarWithDistrib_NE("Y", model),
				new DefaultPartialWorld(), Util.set());

		Sample sample = sampler.sample();

		System.out.println("Sample: " + sample);
	}
}
