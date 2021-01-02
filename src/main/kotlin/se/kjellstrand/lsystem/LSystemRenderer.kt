package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystem
import se.kjellstrand.variablewidthline.LinePoint
import kotlin.math.pow

object LSystemRenderer {

    fun getRecommendedMinAndMaxWidth(size: Int, iteration: Int, def: LSystem): Pair<Double, Double> {
        val maxWidth = (size / (iteration + 1).toDouble()
            .pow(def.lineWidthExp)) * def.lineWidthBold
        val minWidth = maxWidth / 10.0
        return Pair(minWidth, maxWidth)
    }

    fun adjustLineWidthAccordingToImage(
        line: List<LinePoint>,
        luminanceData: Array<ByteArray>,
        outputImageSize: Int,
        minWidth: Double,
        maxWidth: Double
    ) {
        val xScale = outputImageSize.toDouble() / (luminanceData[0].size)
        val yScale = outputImageSize.toDouble() / (luminanceData.size)
        for (element in line) {
            // Use the inverted brightness as width of the line we drawSpline.
            element.w = minWidth +
                    ((luminanceData[element.x.div(xScale).toInt()][element.y.div(yScale).toInt()]+127) / 255.0) *
                    (maxWidth - minWidth)
        }
    }
}