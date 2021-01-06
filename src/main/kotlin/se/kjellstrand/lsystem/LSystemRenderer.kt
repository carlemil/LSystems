package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystem
import kotlin.math.pow

object LSystemRenderer {

    fun getRecommendedMinAndMaxWidth(size: Int, iteration: Int, def: LSystem): Pair<Double, Double> {
        val maxWidth = (size / (iteration + 1).toDouble()
            .pow(def.lineWidthExp)) * def.lineWidthBold
        val minWidth = maxWidth / 10.0
        return Pair(minWidth, maxWidth)
    }

    fun setLineWidthAccordingToImage(
        line: List<Triple<Float, Float, Float>>,
        luminanceData: Array<ByteArray>,
        minWidth: Double,
        maxWidth: Double
    ): List<Triple<Float, Float, Float>> {
        val xScale = luminanceData.size - 1
        val yScale = luminanceData[0].size - 1
        return line.map { p ->
            // Use the inverted brightness as width of the line we drawSpline.
            val lum = luminanceData[(p.first * xScale).toInt()][(p.second * yScale).toInt()]
            Triple(p.first, p.second, (minWidth + ((lum + 127) / 255.0) * (maxWidth - minWidth)).toFloat())
        }
    }
}