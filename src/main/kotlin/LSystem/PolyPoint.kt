package LSystem

import java.awt.Color

class PolyPoint(x: Double, y: Double, width: Double = 1.0, color: Color = Color.BLACK) {
    val x = x
    val y = y
    val w = width
    val c = color

    override fun toString(): String {
        return "x: %.2f".format(x) + ", y: %.2f".format(y) + " c: " + c
    }
}
