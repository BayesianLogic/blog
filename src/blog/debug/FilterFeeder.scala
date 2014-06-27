package blog.debug

import blog.model.Evidence
import blog.model.Queries

/**
 * Feeds evidence and queries to a ParticleFilter.
 *
 * If you have all the evidence and queries ahead of time, use
 * OfflineFilterFeeder. If your filter is online, subclass FilterFeeder
 * to provide the new evidence and queries as time progresses.
 *
 * Each item in the iterator is a (timestep, evidence, queries) triple.
 * The timesteps have to be monotonically increasing. The evidence or
 * queries may be null.
 *
 * @author cberzan
 * @since Jun 23, 2014
 */
abstract class FilterFeeder extends Iterator[(Int, Evidence, Queries)] {

}