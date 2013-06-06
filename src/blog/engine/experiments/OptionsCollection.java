package blog.engine.experiments;

import java.util.List;

import blog.common.cmdline.BooleanOption;
import blog.common.cmdline.IntOption;
import blog.common.cmdline.StringOption;

public class OptionsCollection {
	static StringOption mode;
	static BooleanOption dropHistory;
	static IntOption numParticles;
	static IntOption numTimesteps;
	static StringOption policyFile;
	static StringOption queryFile;
	static BooleanOption breadthFirst;
	static StringOption logName;
	static BooleanOption inverseBucket;
	static BooleanOption userInput;
	static List<String> filenames;
}
