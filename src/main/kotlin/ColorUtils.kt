import java.awt.*

object ColorUtils {

    fun blend(c0: Color, c1: Color, ratio0: Double): Color {
        val ratio1 = 1.0 - ratio0

        val r = ratio0 * c0.red + ratio1 * c1.red
        val g = ratio0 * c0.green + ratio1 * c1.green
        val b = ratio0 * c0.blue + ratio1 * c1.blue
        val a = Math.max(c0.alpha, c1.alpha).toDouble()

        return Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
    }

    fun getHexString(c: Color): String {
        val double = c.rgb//+ c.alpha * Math.pow(2.0, 24.0)
        val string = (double.toInt() and 0xFFFFFF).toString(16)
        return string.padStart(6,'0')
    }
}