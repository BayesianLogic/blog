package blog.debug

import blog.model.Evidence
import blog.model.Queries

/**
 * @author cberzan
 * @since Jun 23, 2014
 */
abstract class FilterFeeder extends Iterator[(Int, Evidence, Queries)] {

  def initialEvidence: Evidence

  def finalQueries: Queries

}