package blog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.model.ArgSpecQuery;
import blog.model.Model;

/**
 * Given a set of query templates (where "t" stands for an arbitrary time step)
 * and a model, provides instantiated queries for given time steps by request.
 * 
 * @author Rodrigo
 */
public class TemporalQueriesInstantiator {

	public TemporalQueriesInstantiator(Model model, Collection queryTemplates) {
		this.model = model;
		this.templates = queryTemplates;
	}

	/** Instantiates queries for a given time step. */
	public Collection getQueries(int t) {
		LinkedList list = new LinkedList();
		templatesByLatestQueries = new HashMap();
		for (Iterator it = templates.iterator(); it.hasNext();) {
			String template = (String) it.next();
			ArgSpecQuery query = getQuery(template, t);
			list.add(query);
			templatesByLatestQueries.put(query, template);
		}
		return list;
	}

	/** Returns the template that generated a given query in the last advance. */
	public String getTemplateOf(ArgSpecQuery query) {
		return (String) templatesByLatestQueries.get(query);
	}

	/** Instantiates a query for given template and time step. */
	public ArgSpecQuery getQuery(String template, int t) {
		return getQuery(template, t, model);
	}

	/** Instantiates a query for given template, time step and model. */
	public static ArgSpecQuery getQuery(String template, int t, Model model) {
		ArgSpecQuery result = null;
		String queryString = getString(template, t);
		result = (ArgSpecQuery) BLOGUtil.parseQuery_NE(queryString, model);
		return result;
	}

	private static Pattern pattern = Pattern.compile("\\bt\\b");

	/** Replaces identifier "t" in given string by given time step. */
	public static String getString(String template, int t) {
		Matcher matcher = pattern.matcher(template);
		return matcher.replaceAll("@" + t);
	}

	protected Model model;
	protected Collection templates;
	private Map templatesByLatestQueries;
}
