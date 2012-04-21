package blog;

import java.util.*;

import blog.common.EZIterator;
import blog.common.HashMultiset;
import blog.common.Multiset;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.TupleSetSpec;


/**
 * Extends {@link TemporalPartialWorldGenerator} in order to generate evidence
 * from it. This is done by successively expanding a partial world and reading
 * data from it that works as evidence. The user must provide a set of link
 * variables that drive the partial world generation. Because there is
 * (currently) no way in BLOG to provide evidence on a cardinality value, this
 * class manages links on cardinalities by querying cardinalities and posing
 * symbol evidence with the exact number of symbols. The user can also provide
 * query strings that will be used to further instantiate the partial world even
 * if they do not create evidence. Note that queries are not actually queried
 * because the generator is not an {@link InferenceEngine}. They are simply to
 * indicate what needs to be instantiated at each time step. They are useful for
 * simply following what is going on in the partial world being generated.
 * 
 * @author Rodrigo
 */
public class TemporalEvidenceGenerator extends TemporalPartialWorldGenerator {

	public TemporalEvidenceGenerator(Model model, Collection linkStrings,
			Collection queryStrings) {
		super(model, makeQueryTemplates(linkStrings, queryStrings));
		this.linkStrings = linkStrings;
		this.queryStrings = queryStrings;
		makeLinkStringsByTemplate();
		makeTagByLinkString();
	}

	/**
	 * Returns a list of query templates necessary for
	 * {@link TemporalPartialWorldGenerator}. These come from both links and query
	 * strings. The templates coming from links are used as indices to the
	 * original link strings in map {@link #linkStringsByTemplate},
	 */
	private static LinkedList makeQueryTemplates(Collection linkStrings,
			Collection queryStrings) {
		LinkedList list = new LinkedList();

		for (Iterator it = linkStrings.iterator(); it.hasNext();) {
			String linkString = (String) it.next();
			String template = "query " + linkString + ";";
			list.add(template);
		}

		for (Iterator it = queryStrings.iterator(); it.hasNext();) {
			String queryString = (String) it.next();
			String template = "query " + queryString + ";";
			list.add(template);
		}

		return list;
	}

	/**
	 * Returns a list of query templates necessary for
	 * {@link TemporalPartialWorldGenerator}. These come from both links and query
	 * strings. The templates coming from links are used as indices to the
	 * original link strings in map {@link #linkStringsByTemplate},
	 */
	private void makeLinkStringsByTemplate() {
		linkStringsByTemplate = new HashMap();
		for (Iterator it = linkStrings.iterator(); it.hasNext();) {
			String linkString = (String) it.next();
			String template = "query " + linkString + ";";
			linkStringsByTemplate.put(template, linkString);
		}
	}

	/** Returns evidence for current time step. */
	public Evidence getEvidence() {
		Evidence evidence = new Evidence();

		if (getLatestQueries() == null)
			Util.fatalError("TemporalEvidenceGenerator.getEvidence() invoked without previous moveOn invocation.");

		for (Iterator it = getLatestQueries().iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();

			if (linkStringsByTemplate.containsKey(getTemplateOf(query))) // if link
				evidence.addAll(getLinkEvidence(query));
		}
		return evidence;
	}

	/**
	 * Provides evidence for a given link, which can be symbol evidence if link is
	 * on a cardinality, or value evidence if it is on a regular variable.
	 */
	private Evidence getLinkEvidence(ArgSpecQuery linkQuery) {
		String variableString = linkQuery.getVariable().toString();
		if (variableString.indexOf('#') != -1)
			return getCardinalityLinkEvidence(linkQuery);
		else {
			Object value = currentPartialWorld.getValue(linkQuery.getVariable());
			String evidenceString = "obs " + linkQuery.getVariable() + " = "
					+ valueRepresentation(linkQuery, value) + ";";
			Evidence evidence = BLOGUtil.parseEvidence_NE(evidenceString, model);
			evidence.compile();
			return evidence;
		}
	}

	/**
	 * Produces the adequate representation for an value, depending on variable
	 * type." If the link is a TupleSetSpec query, the value has to be formatted
	 * to be fit for a TupleSetSpec value evidence statement according to BLOG
	 * syntax (with curly rather than square brackets). Also note that a level of
	 * brackets needs to be removed. This is because a TupleSetSpec with
	 * 1-dimensional tuples is indistinguishable from the way we write set
	 * specifications in math. In other words, when the user writes a value
	 * evidence statement using a TupleSetSpec with 1-dimensional tuples on the
	 * left side, they are probably expecting that to be taken as a set of
	 * elements, not of 1-dimensional tuples. It seems to me that we should change
	 * the BLOG syntax to have explicit parenthesis around tuples, distinguishing
	 * them from elements.
	 */
	private String valueRepresentation(ArgSpecQuery linkQuery, Object value) {
		if (linkQuery.argSpec instanceof TupleSetSpec) {
			Collection valueAsCollection = (Collection) value;
			Multiset elements = new HashMultiset();
			for (Object element : valueAsCollection) {
				Collection tuple = (Collection) element;
				if (tuple.size() > 1)
					Util.fatalError("Tuple with dimension greater than 1 used in link for TemporalEvidenceGenerator.");
				elements.add(Util.getFirst(tuple));
			}
			return "{" + Util.join(elements) + "}";
		}
		return value.toString();
	}

	private Evidence getCardinalityLinkEvidence(ArgSpecQuery linkQuery) {
		Object cardinalityValue = currentPartialWorld.getValue(linkQuery
				.getVariable());
		String queryTemplate = getTemplateOf(linkQuery);
		String cardinalityString = (String) linkStringsByTemplate
				.get(queryTemplate);
		Evidence linkEvidence = getCardinalityLinkEvidence(cardinalityString,
				cardinalityValue);
		linkEvidence.compile();
		return linkEvidence;
	}

	private Evidence getCardinalityLinkEvidence(String cardinalityString,
			Object cardinalityValue) {
		return getCardinalityLinkEvidence(cardinalityString, "SetLink"
				+ tagByLinkString.get(cardinalityString), cardinalityValue);
	}

	protected Evidence getCardinalityLinkEvidence(String cardinalityString,
			String prefix, Object value) {
		String symbolList = makeSymbolList(prefix, lastTimeStep,
				((Number) value).intValue());

		String evidenceString = "obs "
				+ DBLOGUtil.replaceTByTimeStep(cardinalityString.substring(1),
						lastTimeStep) + " = " + symbolList + ";";

		Evidence evidence = BLOGUtil.parseEvidence_NE(evidenceString, model);

		return evidence;
	}

	/**
	 * Creates an arbitrary integer tag for each link string, stored in
	 * {@link #tagByLinkString}, used to distinguish symbol evidence identifiers
	 * from different links.
	 */
	private void makeTagByLinkString() {
		tagByLinkString = new HashMap();
		int order = 0;
		for (Iterator it = linkStrings.iterator(); it.hasNext();) {
			tagByLinkString.put(it.next(), order++);
		}
	}

	private String makeSymbolList(String prefix, int timestep, int number) {
		List symbols = new LinkedList();
		prefix = prefix + Integer.toString(timestep) + "_";
		for (int i = 0; i != number; i++)
			symbols.add(prefix + Integer.toString(i));
		return "{" + Util.join(", ", symbols) + "}";
	}

	/**
	 * Returns evidence generated by a newly built TemporalEvidenceGenerator up to
	 * a given time step, for a given model and collection of link strings.
	 */
	public static Evidence collectEvidenceUpTo(int timestepIndex, Model model,
			Collection linkStrings) {
		TemporalEvidenceGenerator generator = new TemporalEvidenceGenerator(model,
				linkStrings, Util.list());
		return generator.collectEvidenceUpTo(timestepIndex);
	}

	/**
	 * Returns evidence generated up to a given time step.
	 */
	public Evidence collectEvidenceUpTo(int timestepIndex) {
		Evidence result = new Evidence();
		while (lastTimeStep != timestepIndex) {
			moveOn();
			result.addAll(getEvidence());
		}
		return result;
	}

	/**
	 * An iterator class ranging over evidence from a
	 * {@link TemporalEvidenceGenerator}.
	 */
	public static class EvidenceIterator extends EZIterator {
		public EvidenceIterator(Model model, Collection linkStrings) {
			generator = new TemporalEvidenceGenerator(model, linkStrings, Util.list());
		}

		protected Object calculateNext() {
			generator.moveOn();
			return generator.getEvidence();
		}

		private TemporalEvidenceGenerator generator;
	}

	protected Collection linkStrings;
	protected Collection queryStrings;

	/**
	 * A map from link strings to arbitrary integer tags, used to differentiate
	 * symbol evidence identifiers.
	 */
	protected Map tagByLinkString;

	/**
	 * We keep a map between templates and linkStrings as a bridge between
	 * instantiated queries and links (the generator provides the template for an
	 * instantiated query), so that we can generate evidence for the link's
	 * variables using the query's value.
	 */
	protected Map linkStringsByTemplate;

	protected int id;
}
