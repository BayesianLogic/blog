package blog.debug

import scala.collection.JavaConversions._
import scala.collection.mutable
import blog.model.Evidence
import blog.model.Queries
import blog.DBLOGUtil
import blog.`type`.Timestep
import scala.collection.mutable.ListBuffer

/**
 * @author cberzan
 * @since Jun 25, 2014
 */
class OfflineFilterFeeder(allEvidence: Evidence, allQueries: Queries) extends FilterFeeder {

  val items: List[(Int, Evidence, Queries)] = {
    val slicedEvidence = DBLOGUtil.splitEvidenceInTime(allEvidence)
    val slicedQueries = DBLOGUtil.splitQueriesInTime(allQueries)
    val timesteps = ((slicedEvidence.keySet() ++ slicedQueries.keySet()) - null).toList.sorted

    // We construct the result in tmp.
    val tmp = new ListBuffer[(Int, Evidence, Queries)]

    // Atemporal evidence, if any:
    val initialEvidence = slicedEvidence.getOrElse(null, null)
    if (initialEvidence != null) {
      tmp.append((-1, initialEvidence, null))
    }

    // Evidence and queries for every timestep:
    timesteps.foreach(timestep => {
      val evidence = slicedEvidence.getOrElse(timestep, null)
      val queries = slicedQueries.getOrElse(timestep, null)
      tmp.append((timestep.getValue, evidence, queries))
    })

    // Atemporal queries, if any:
    val finalQueries = slicedQueries.getOrElse(null, null)
    if (finalQueries != null) {
      tmp.append((timesteps.last.getValue, null, finalQueries))
    }

    tmp.toList
  }
  val iterator = items.iterator

  def hasNext = iterator.hasNext
  def next = iterator.next
}