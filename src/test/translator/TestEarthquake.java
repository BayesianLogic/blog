package test.translator;

import blog.translate.FigaroTranslator;

/**
 * compute example < Burglary-earthquake network > from BLOG website
 * 
 * Note : 
 *        BLOG file is stored as /example/translator/earthquake.blog
 *        Output will be /example/translator/earthquake.scala
 *                       /example/translator/earthquake_structure_log.txt
 *        
 * @author Yi Wu
 * @date Oct 6, 2012
 */

public class TestEarthquake {

	static String[] param={"Translator","example/translator/earthquake.blog",
		"example/translator/earthquake.scala","example/translator/earthquake_structure_log.txt"};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FigaroTranslator trans = new FigaroTranslator();
		
		trans.trainslate(param);
	}

}
