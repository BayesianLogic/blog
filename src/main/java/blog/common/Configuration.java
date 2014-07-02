/**
 * 
 */
package blog.common;

import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class.
 * 
 * There will be only One instance of configuration, created by Singleton
 * method.
 * 
 * @author leili
 * @date Apr 22, 2012
 */
public class Configuration {

	private Properties properties;

	/**
	 * singleton
	 */
	private static final Configuration cfg = new Configuration();

	/**
	 * private constructor
	 */
	private Configuration() {

		properties = getDefaultProperties();
		InputStream is = null;
		try {
			// is = new FileInputStream(System.getProperty("user.dir") +
			// "/femine.conf");
			// the file should be in blog.util
			is = getClass().getResourceAsStream("blog.conf");
			if (is == null) {
				System.out
						.println("Config file not found!\nUsing default properties instead.");
			} else {
				properties.load(is);
			}
		} catch (Exception e) {
			System.err.println("Can't read the properties file.");
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Properties getDefaultProperties() {
		Properties prop = new Properties();
		prop.setProperty("engineClass", "blog.engine.SamplingEngine");
		prop.setProperty("numSamples", "10000");
		prop.setProperty("samplerClass", "blog.sample.LWSampler");
		prop.setProperty("proposerClass", "blog.GenericProposer");
		return prop;
	}

	/**
	 * return an instance
	 * 
	 * @return Configuration
	 */
	public static Configuration getInstance() {
		return cfg;
	}

	/**
	 * get value key
	 * 
	 * @param key
	 *          String
	 * @return String
	 */
	public String getValue(String key) {
		return properties.getProperty(key);
	}
}
