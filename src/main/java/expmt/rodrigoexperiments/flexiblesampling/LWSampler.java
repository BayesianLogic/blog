package expmt.rodrigoexperiments.flexiblesampling;

import java.util.Properties;
import java.util.Set;

import blog.BLOGUtil;
import blog.common.Util;
import blog.model.Model;
import blog.sample.Sampler;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;


public class LWSampler extends Sampler {

	public LWSampler(Model model, Properties properties) {
		super(model);
		String idTypesString = properties.getProperty("idTypes", "none");
		idTypes = model.getListedTypes(idTypesString);
		if (idTypes == null) {
			Util.fatalErrorWithoutStack("Invalid idTypes list.");
		}
		this.flexibleImportanceSamplerFactory = new DefaultSamplerFactory();
	}

	@Override
	public void setBaseWorld(PartialWorld world) {
		baseWorld = world;
	}

	@Override
	public void nextSample() {
		if (baseWorld == null)
			currentWorld = new DefaultPartialWorld(idTypes);
		else
			currentWorld = baseWorld;

		BLOGUtil.setBasicVars(evidence, currentWorld);

		context = new EvalContext(currentWorld, Util.set(),
				flexibleImportanceSamplerFactory);

		BLOGUtil.ensureDetAndSupported(BLOGUtil.getQueriesVars(queries), context);
		BLOGUtil.ensureDetAndSupported(evidence.getEvidenceVars(), context);
	}

	@Override
	public PartialWorld getLatestWorld() {
		return currentWorld;
	}

	@Override
	public double getLatestLogWeight() {
		if (context == null) {
			throw new IllegalStateException("LWSampler has no latest sample.");
		}
		return Math.log(context.getWeight() * evidence.getEvidenceProb(currentWorld));
		// TODO: OPTIMIZATION: if a evidence variable's parents are evidence
		// variables themselves, their probability can be factored out from here.
		// Also, this can be cached in case getLatestLogWeight gets called more than
		// once in between samples.
	}

	private Set idTypes; // of Type
	private PartialWorld baseWorld;
	private PartialWorld currentWorld;
	private SamplerFactory flexibleImportanceSamplerFactory;
	private EvalContext context;
}
