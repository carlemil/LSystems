import java.awt.*

object ColorUtils {

    fun blend(i0: Int, i1: Int): Color {
        val c0 = Color(i0)
        val c1 = Color(i1)
        return blend(c0, c1)
    }

    fun blend(c0: Color, c1: Color): Color {
        val totalAlpha = (c0.alpha + c1.alpha).toDouble()
        val weight0 = c0.alpha / totalAlpha
        val weight1 = c1.alpha / totalAlpha

        val r = weight0 * c0.red + weight1 * c1.red
        val g = weight0 * c0.green + weight1 * c1.green
        val b = weight0 * c0.blue + weight1 * c1.blue
        val a = Math.max(c0.alpha, c1.alpha).toDouble()

        return Color(r.toInt(), g.toInt(), b.toInt(), a.toInt())
    }
}