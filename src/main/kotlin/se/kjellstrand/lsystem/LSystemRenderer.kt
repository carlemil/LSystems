package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystemDefinition
import se.kjellstrand.variablewidthline.LinePoint
import se.kjellstrand.variablewidthline.VariableWidthLine
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.pow

object LSystemRenderer {

    fun renderLSystem(
        line: List<LinePoint>,
        brightnessImage: BufferedImage,
        outputImageSize: Double,
        minWidth: Double,
        maxWidth: Double
    ): BufferedImage {

        adjustWidthAccordingToImage(line, brightnessImage)

        val sidePadding = (maxWidth / 2.0) / outputImageSize + 0.02

        val (bufferedImage, g2) = setupGraphics(outputImageSize + outputImageSize * sidePadding * 2.0)

        line.forEach { lp ->
            lp.x += sidePadding
            lp.y += sidePadding
        }

        VariableWidthLine.drawLine(line, g2, outputImageSize, minWidth, maxWidth)

        tearDownGraphics(g2)

        return bufferedImage
    }

    fun getMinAndMaxWidth(size: Double, iteration: Int, def: LSystemDefinition): Pair<Double, Double> {
        val maxWidth = (size / (iteration + 1).toDouble()
            .pow(def.lineWidthExp)) * def.lineWidthBold
        val minWidth = maxWidth / 10.0
        return Pair(maxWidth, minWidth)
    }

    private fun tearDownGraphics(g2: Graphics2D) {
        g2.dispose()
    }

    private fun adjustWidthAccordingToImage(line: List<LinePoint>, image: BufferedImage?) {
        for (element in line) {
            // Use the inverted brightness as width of the line we drawSpline.
            element.w = (1 - getBrightnessFromImage(element.x, element.y, image))
        }
    }

    private fun getBrightnessFromImage(x_: Double, y_: Double, image: BufferedImage?): Double {
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
            c
        )
        return c[2].toDouble()
    }

    private fun setupGraphics(size: Double): Pair<BufferedImage, Graphics2D> {
        val bufferedImage = BufferedImage((size).toInt(), (size).toInt(), BufferedImage.TYPE_INT_RGB)

        val g2 = bufferedImage.createGraphics()
        val rh = mutableMapOf<RenderingHints.Key, Any>()
        rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
        rh[RenderingHints.KEY_ALPHA_INTERPOLATION] = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        rh[RenderingHints.KEY_COLOR_RENDERING] = RenderingHints.VALUE_COLOR_RENDER_QUALITY
        rh[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
        rh[RenderingHints.KEY_STROKE_CONTROL] = RenderingHints.VALUE_STROKE_PURE
        g2.setRenderingHints(rh)

        g2.stroke = BasicStroke(2f)
        g2.color = Color.WHITE
        g2.fill(Rectangle(0, 0, bufferedImage.width, bufferedImage.height))
        return Pair(bufferedImage, g2)
    }
}