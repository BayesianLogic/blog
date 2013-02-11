package test.blog;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import blog.BLOGUtil;
import blog.TemporalEvidenceGenerator;
import blog.TemporalQueriesInstantiator;
import blog.common.UnaryFunction;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;


/**
 * Grouping class for a model, evidence and queries, link string (see
 * {@link TemporalEvidenceGenerator}) and a query template (see
 * {@link TemporalQueriesInstantiator}), used in {@link DBLOGExperiments}.
 */
class Problem {

	public Problem(Model model, Evidence evidence, List queries,
			Collection linkStrings, String queryTemplate) {
		this.model = model;
		this.evidence = evidence;
		this.queries = queries;
		this.linkStrings = linkStrings;
		this.queryTemplate = queryTemplate;
	}

	/**
	 * Builds problem from String descriptions. Note that <code>queryString</code>
	 * must describe a single query only.
	 */
	public Problem(String modelDescription, String evidenceString,
			String queryString, Collection linkStrings, String queryTemplate) {
		this.model = BLOGUtil.parseModel_NE(modelDescription);
		this.evidence = BLOGUtil.parseEvidence_NE(evidenceString, model);
		this.queries = BLOGUtil.parseQueries_NE(queryString, model);
		this.linkStrings = linkStrings;
		this.queryTemplate = queryTemplate;
	}

	/**
	 * Same as {@link #Problem(String, String, String, Collection, String)}, but
	 * accepting a normalizer (for the query) as well.
	 */
	public Problem(String modelDescription, String evidenceString,
			String queryString, Collection linkStrings, String queryTemplate,
			UnaryFunction normalizer) {
		this(modelDescription, evidenceString, queryString, linkStrings,
				queryTemplate);
		this.normalizer = normalizer;
		if (normalizer != null && query() != null) {
			query().setNormalizer(normalizer);
		}
	}

	public Problem(Problem another) {
		this.model = another.model;
		this.evidence = another.evidence;
		this.queries = Util.list();
		for (Iterator it = another.queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			this.queries.add(new ArgSpecQuery(query));
		} // duplicate queries because results are stored in them
		this.queries = another.queries;
		this.linkStrings = another.linkStrings;
		this.queryTemplate = another.queryTemplate;
		this.normalizer = another.normalizer;
	}

	/**
	 * Convenience method assuming there is only one query, of class ArgSpecQuery,
	 * and returning it, or null if there aren't any queries.
	 */
	public ArgSpecQuery query() {
		return (ArgSpecQuery) Util.getFirstOrNull(queries);
	}

	/**
	 * Returns a problem which instantiates this problem with a query and evidence
	 * according to a given time step.
	 */
	public Problem getRandomProblem(int t) {
		Problem result = new Problem(this);
		result.instantiateQueryTo(t);
		result.evidence = TemporalEvidenceGenerator.collectEvidenceUpTo(t - 1,
				model, linkStrings);
		return result;
	}

	private void instantiateQueryTo(int t) {
		queries = Util.list(getInstantiatedQuery(t));
	}

	/**
	 * Returns the query template instantiated to a given time step index.
	 */
	public ArgSpecQuery getInstantiatedQuery(int t) {
		ArgSpecQuery result = TemporalQueriesInstantiator.getQuery(queryTemplate,
				t, model);
		if (normalizer != null) {
			result.setNormalizer(normalizer);
		}
		return result;
	}

	public Model model;
	public Evidence evidence;
	public List queries;
	public Collection linkStrings;
	public String queryTemplate;
	public UnaryFunction normalizer;
}