package se.kjellstrand.lsystem

import se.kjellstrand.variablewidthpolygon.PolygonPoint
import se.kjellstrand.variablewidthpolygon.VariableWidthPolygon
import java.awt.*
import java.awt.image.BufferedImage

object LSystemRenderer {

    fun renderLSystem(polygon: List<PolygonPoint>,
                      brightnessImage: BufferedImage,
                      outputImageSize: Double): BufferedImage {
        val t0 = System.currentTimeMillis()

        adjustWidthAccordingToImage(polygon, brightnessImage)

        val sidePadding = VariableWidthPolygon.calculateSidesOfTriangle(polygon[2], polygon[3]).third *
                outputImageSize / 5 + outputImageSize / 60

        val (bufferedImage, g2) = setupGraphics(outputImageSize, sidePadding)

        VariableWidthPolygon.drawPolygonToBufferedImage(polygon, g2, outputImageSize, sidePadding)
        // lSystem.polygon.VariableWidthPolygon.drawDebugPolygon(polygon, g2, outputImageSize, sidePadding)
        VariableWidthPolygon.tearDownGraphics(g2)

        val t1 = System.currentTimeMillis()
        println("Render polygon in total: " + (t1 - t0) + "ms\n")

        return bufferedImage
    }

    private fun adjustWidthAccordingToImage(polygon: List<PolygonPoint>, image: BufferedImage?) {
        for (element in polygon) {
            // Use the inverted brightness as width of the line we drawSpline.
            element.w = (1 - ColorUtils.getBrightnessFromImage(element.x, element.y, image))
        }
    }

    private fun setupGraphics(size: Double, sidePadding: Double): Pair<BufferedImage, Graphics2D> {
        val bufferedImage = BufferedImage((size + sidePadding * 2).toInt(), (size + sidePadding * 2).toInt(),
                BufferedImage.TYPE_INT_RGB)

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