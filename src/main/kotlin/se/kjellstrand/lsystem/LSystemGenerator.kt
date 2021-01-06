package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystem
import java.lang.Math.PI
import java.util.*
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 */
object LSystemGenerator {

    fun generatePolygon(lSystem: LSystem, iterations: Int): List<Pair<Float, Float>> {
        var instructions =
            generate(lSystem.axiom, lSystem.rules, iterations, lSystem.forwardChars)

        val xyList = convertToPolyPointList(
            instructions.toString(),
            lSystem.getAngleInRadians(),
            lSystem.forwardChars
        )

        val scaledList = scalePolyPointList(xyList)

        return smoothenTheLine(scaledList)
    }

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

    private fun generate(
        axiom: String,
        rules: Map<String, String>,
        iterations: Int,
        forwardChars: Set<String>
    ): StringBuilder {
        val tmp = StringBuilder()
        tmp.append(axiom)
        val instructions = StringBuilder()
        for (i in 1..iterations) {
            instructions.setLength(0)
            for (c in tmp) {
                val cs = c.toString()
                if (cs in rules) {
                    instructions.append(rules[c.toString()])
                } else {
                    instructions.append(cs)
                }
            }
            tmp.setLength(0)
            tmp.append(instructions)
        }
        for (c in forwardChars) {
            instructions.replace(Regex(c), "F")
        }
        return instructions
    }

    private fun convertToPolyPointList(
        instructions: String,
        systemAngle: Float,
        forwardChars: Set<String>
    ): List<Pair<Float, Float>> {
        val list: MutableList<Pair<Float, Float>> = mutableListOf()

        var x = 0.0F
        var y = 0.0F
        var angle: Float = (PI / 2).toFloat()

        val stack: Stack<Pair<Float, Float>> = Stack()

        list.add(Pair(x, y))
        for (c in instructions) {
            when (c.toString()) {
                "-" -> angle -= systemAngle
                "+" -> angle += systemAngle
                "[" -> stack.push(Pair(x, y))
                "]" -> {
                    val p = stack.pop()
                    x = p.first
                    y = p.second
                }
                in forwardChars -> {
                    x += kotlin.math.sin(angle)
                    y += kotlin.math.cos(angle)
                    list.add(Pair(x, y))
                }
            }
        }
        return list
    }

    private fun scalePolyPointList(list: List<Pair<Float, Float>>): MutableList<Pair<Float, Float>> {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (p in list) {
            if (p.first < minX) minX = p.first
            if (p.second < minY) minY = p.second

            if (p.first > maxX) maxX = p.first
            if (p.second > maxY) maxY = p.second
        }

        val scaleX = 1 / (maxX - minX)
        val scaleY = 1 / (maxY - minY)

        val scale = kotlin.math.min(scaleX, scaleY)

        val xSpace = maxX - minX
        val ySpace = maxY - minY
        val offsetX = if (xSpace < ySpace) (ySpace - xSpace) / 2.0F else 0.0F
        val offsetY = if (ySpace < xSpace) (xSpace - ySpace) / 2.0F else 0.0F

        val scaledList: MutableList<Pair<Float, Float>> = mutableListOf()
        for (p in list) {
            scaledList.add(Pair((p.first - minX + offsetX) * scale, (p.second - minY + offsetY) * scale))
        }

        return scaledList
    }

    private fun smoothenTheLine(list: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        val smoothedList: MutableList<Pair<Float, Float>> = mutableListOf()
        for (i in -1 until list.size) {
            val p01 = list[(i - 1).coerceAtLeast(0)]
            val p02 = list[i.coerceAtLeast(0)]
            val p03 = list[(i + 1).coerceAtMost(list.size - 1)]
            addSplineBetweenPoints(
                getMidPoint(p01, p02),
                p02,
                getMidPoint(p02, p03),
                smoothedList
            )
        }
        return smoothedList
    }

    private fun getMidPoint(p0: Pair<Float, Float>, p1: Pair<Float, Float>): Pair<Float, Float> {
        return Pair((p0.first + p1.first) / 2.0F, (p0.second + p1.second) / 2.0F)
    }

    private fun addSplineBetweenPoints(
        pp1: Pair<Float, Float>, pp2: Pair<Float, Float>, pp3: Pair<Float, Float>,
        outputList: MutableList<Pair<Float, Float>>
    ) {
        val tincrement = 1.0 / 5.0
        var t = 0.0
        while (t < (1.0 - (tincrement / 2.0))) {
            // The bezier square spline is calculated using this formula from wikipedia.
            // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

            // Calculate the Bezier (x, y) coordinate for this step.
            val x = ((1 - t).pow(2.0) * pp1.first) +
                    (2 * (1 - t) * t * pp2.first) +
                    (t.pow(2.0) * pp3.first)
            val y = ((1 - t).pow(2.0) * pp1.second) +
                    (2 * (1 - t) * t * pp2.second) +
                    (t.pow(2.0) * pp3.second)

            outputList.add(Pair(x.toFloat(), y.toFloat()))

            // Calculate the t value used in the Bezier calculations above.
            // Dont change the / X value here without updating in lSystem.polygon.VariableWidthPolygon.calculatePerpendicularPolyPoint()
            t += tincrement
        }
    }

}