package se.kjellstrand.variablewidthline

class LinePoint(var x: Double, var y: Double, var w: Double = 1.0) {

    companion object {
        fun getMidPoint(p0: LinePoint, p1: LinePoint): LinePoint {
            return LinePoint((p0.x + p1.x) / 2.0, (p0.y + p1.y) / 2.0, (p0.w + p1.w) / 2.0)
        }
    }

    override fun toString(): String {
        return "x: %.2f".format(x) + ", y: %.2f".format(y) + " w: %.2f".format(w)
    }
}
