package blog.debug

import scala.collection.JavaConversions._
import scala.collection.mutable

import blog.model.Evidence
import blog.model.Queries
import blog.DBLOGUtil
import blog.`type`.Timestep

/**
 * @author cberzan
 * @since Jun 25, 2014
 */
class OfflineFilterFeeder(allEvidence: Evidence, allQueries: Queries) extends FilterFeeder {

  val slicedEvidence = DBLOGUtil.splitEvidenceInTime(allEvidence)
  val slicedQueries = DBLOGUtil.splitQueriesInTime(allQueries)
  val timesteps = {
    val tmp = (slicedEvidence.keySet() ++ slicedQueries.keySet()) - null
    tmp.toList.sorted
  }
  val timestepIterator = timesteps.iterator

  def hasNext = timestepIterator.hasNext

  def next = {
    val timestep = timestepIterator.next
    val evidence = slicedEvidence.getOrElse(timestep, null)
    val queries = slicedQueries.getOrElse(timestep, null)
    (timestep.getValue(), evidence, queries)
  }

  def initialEvidence = slicedEvidence.getOrElse(null, null)

  def finalQueries = slicedQueries.getOrElse(null, null)
}