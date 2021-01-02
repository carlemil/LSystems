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
        minWidth: Double,
        maxWidth: Double
    ) {
        val xScale = luminanceData.size - 1
        val yScale = luminanceData[0].size - 1
        for (p in line) {
            // Use the inverted brightness as width of the line we drawSpline.
            val lum = luminanceData[(p.x * xScale).toInt()][(p.y * yScale).toInt()]
            p.w = minWidth + ((lum + 127) / 255.0) * (maxWidth - minWidth)
        }
    }
}