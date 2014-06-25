package blog.debug

import blog.model.Evidence
import blog.model.Queries

/**
 * @author cberzan
 * @since Jun 23, 2014
 */
abstract class ParticleFilterFeeder extends Iterator[(Int, Evidence, Queries)] {

}