/**
 * 
 */
package blog;

import java.util.LinkedList;
import java.util.List;

import common.Util;

public class ModelEvidenceQueries {
	public ModelEvidenceQueries() {
		model = new Model();
		evidence = new Evidence();
		queries = new LinkedList();
	}

	public ModelEvidenceQueries(Model model, Evidence evidence, List queries) {
		this.model = model;
		this.evidence = evidence;
		this.queries = queries;
	}

	public ModelEvidenceQueries(String modelDescription, String evidenceString,
			String queriesString) {
		model = BLOGUtil.parseModel_NE(modelDescription);
		evidence = BLOGUtil.parseEvidence_NE(evidenceString, model);
		queries = Util.list(BLOGUtil.parseQuery_NE(queriesString, model));
	}

	/**
	 * Convenience method assuming there is only one query, of class ArgSpecQuery,
	 * and returning it.
	 */
	public ArgSpecQuery argSpecQuery() {
		return (ArgSpecQuery) Util.getFirst(queries);
	}

	public Model model;
	public Evidence evidence;
	public List queries;
}