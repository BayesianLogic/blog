import blog.BLOGUtil
import blog.model.Model
import blog.world.PartialWorld

class LWSample(model: Model, world: PartialWorld, logWeight: Double) {
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

    override def toString = s"logWeight: ${logWeight}  world: ${world}"
}
