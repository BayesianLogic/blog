package blog.debug

import blog.world.PartialWorld
import blog.model.Model
import blog.BLOGUtil

/**
 * A generic sample.
 *
 * Holds a PartialWorld and enables evaluating arbitrary expressions in that world.
 *
 * Concrete subclasses must implement the following methods:
 * - logWeight
 *
 * @author cberzan
 * @since Jun 23, 2014
 *
 */
abstract class Sample(val model: Model, val world: PartialWorld) {
  def eval(exprStr: String): Object = {
    var result: Object = null

    // Try basic lookup first.
    if (result == null) {
      val variable = world.getBasicVarByName(exprStr)
      if (variable != null) {
        result = world.getValue(variable)
      }
    }

    // Then try evaluating an arbitrary expression.
    if (result == null) {
      val expression = BLOGUtil.parseArgSpec(exprStr, model)
      if (expression != null) {
        result = expression.evaluate(world)
      }
    }

    result
  }

  // Log-weight of this sample when updating queries.
  def logWeight: Double

  override def toString = s"Sample(world: ${world})"
}