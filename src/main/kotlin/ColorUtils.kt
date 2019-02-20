import LSystem.color.Palette
import java.awt.*
import java.awt.image.BufferedImage

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
        val string = (double and 0xFFFFFF).toString(16)
        return string.padStart(6, '0')
    }

    fun getLightnessFromImage(x_: Double, y_: Double, image: BufferedImage?): Double {
        var color = 0xffffff
        if (image != null) {
            val x = (x_ * (image.width - 1))
            val y = (y_ * (image.height - 1))
            color = image.getRGB(x.toInt(), y.toInt())
        }
        var c = FloatArray(3)
        Color.RGBtoHSB(
                color shr 16 and 255,
                color shr 8 and 255,
                color and 255,
                c)
        return c[2].toDouble()
    }

    fun getHueFromImage(x_: Double, y_: Double, image: BufferedImage?): Float {
        var color = 0xffffff
        if (image != null) {
            val x = (x_ * (image.width - 1))
            val y = (y_ * (image.height - 1))
            color = image.getRGB(x.toInt(), y.toInt())
        }
        var c = FloatArray(3)
        Color.RGBtoHSB(
                color shr 16 and 255,
                color shr 8 and 255,
                color and 255,
                c)
        return c[1]
    }

    fun getColorFromImage(x_: Double, y_: Double, image: BufferedImage?): Color {
        var color = 0xff10ff
        if (image != null) {
            val x = (x_ * (image.width - 1))
            val y = (y_ * (image.height - 1))
            color = image.getRGB(x.toInt(), y.toInt())
        }
        return Color.decode(color.toString())
    }

    fun getLineSegmentColor(useVariableLineWidth: Boolean, i: Int, brightness: Double, palette: IntArray,
                                    paletteRepeat: Double, xyList: List<Pair<Double, Double>>): String {
        return if (useVariableLineWidth) {
            ColorUtils.getHexString(getPaletteColorByLinePosition(i.toDouble() / xyList.size, brightness, palette, paletteRepeat))
        } else {
            "FF000000"
        }
    }

    fun getPaletteColorByLinePosition(linePosition: Double, brightness: Double, palette: IntArray, paletteRepeat: Double): Color {
        val a = 255
        val f3 = Palette.rgbToFloat3(palette[((linePosition * palette.size) * paletteRepeat).toInt() % palette.size])
        val r = (f3[2] * brightness).toInt()
        val g = (f3[1] * brightness).toInt()
        val b = (f3[0] * brightness).toInt()
        return Color(r, g, b, a)
    }
}