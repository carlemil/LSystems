package se.kjellstrand.variablewidthpolygon

class PolygonPoint(var x: Double, var y: Double, var w: Double = 1.0) {

    companion object {
        fun getMidPoint(p0: PolygonPoint, p1: PolygonPoint): PolygonPoint {
            return PolygonPoint((p0.x + p1.x) / 2.0, (p0.y + p1.y) / 2.0, (p0.w + p1.w) / 2.0)
        }
    }

    override fun toString(): String {
        return "x: %.2f".format(x) + ", y: %.2f".format(y) + " w: %.2f".format(w)
    }
}
