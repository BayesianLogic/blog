package test.translator;

import blog.translate.FigaroTranslator;

/**
 * compute example < Hurricane Preparedness > from BLOG website
 * 
 * Note : This example should be preprocessed! 
 *        The modified file is stored as /example/translator/hurricane.blog
 *        Output will be /example/translator/hurricane.scala
 *                       /example/translator/hurricane_structure_log.txt
 *        
 * @author Yi Wu
 * @date Sept 6, 2012
 */

public class TestHurricane {

	static String[] param={"Translator","example/translator/hurricane.blog",
		"example/translator/hurricane.scala","example/translator/hurricane_structure_log.txt"};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FigaroTranslator trans = new FigaroTranslator();
		
		trans.trainslate(param);
	}

}
