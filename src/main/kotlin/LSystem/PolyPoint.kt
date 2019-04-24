package LSystem

import java.awt.Color

class PolyPoint(x: Double, y: Double, width: Double = 1.0, color: Color = Color.BLACK) {
    val x = x
    val y = y
    val w = width
    val c = color

    companion object {
        fun average(p0: PolyPoint, p1: PolyPoint): PolyPoint {
            return PolyPoint((p0.x + p1.x) / 2.0, (p0.y + p1.y) / 2.0, (p0.w + p1.w) / 2.0, ColorUtils.blend(p0.c, p1.c, 0.5))
        }
    }

    override fun toString(): String {
        return "x: %.2f".format(x) + ", y: %.2f".format(y) + " c: " + c
    }
}
