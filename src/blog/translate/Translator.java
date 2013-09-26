/**
 * 
 */
package blog.translate;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 * static method to be called
 */
public class Translator {

	public static void main(String[] args) {
		FigaroTranslator trans = new FigaroTranslator();
		
		if(!trans.trainslate(args)) System.exit(1);
	}
}
