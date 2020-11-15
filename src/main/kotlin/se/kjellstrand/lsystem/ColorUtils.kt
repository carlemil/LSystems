package se.kjellstrand.lsystem

import java.awt.*
import java.awt.image.BufferedImage
import java.lang.Exception

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
        val string = (c.rgb and 0xFFFFFF).toString(16)
        return string.padStart(6, '0')
    }

    fun getBrightnessFromImage(x_: Double, y_: Double, image: BufferedImage?): Double {
        var color = 0x777777
        if (image != null) {
            val x = (x_ * (image.width - 1))
            val y = (y_ * (image.height - 1))
            try {
                color = image.getRGB(x.toInt(), y.toInt())
            } catch (e: Exception) {
            }
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
        var color = 0x000000
        if (image != null) {
            val x = (x_ * (image.width - 1))
            val y = (y_ * (image.height - 1))
            color = image.getRGB(x.toInt(), y.toInt())
        }
        return Color.decode(color.toString())
    }

}