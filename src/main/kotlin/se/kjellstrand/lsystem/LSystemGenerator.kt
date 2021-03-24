package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSTriple
import se.kjellstrand.lsystem.model.LSystem
import java.lang.Math.PI
import java.util.*
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 */
object LSystemGenerator {

    fun generatePolygon(lSystem: LSystem, iterations: Int): MutableList<LSTriple> {
        var instructions =
            generate(lSystem.axiom, lSystem.rules, iterations, lSystem.forwardChars)

        val xyList = convertToPolyPointList(
            instructions.toString(),
            lSystem.getAngleInRadians(),
            lSystem.forwardChars
        )

        val scaledList = scalePolyPointList(xyList)
        return if (lSystem.intermediateSplines == 0) {
            scaledList
        } else {
            smoothenCurvatureOfLine(scaledList, lSystem.intermediateSplines)
        }
    }

    fun getRecommendedMinAndMaxWidth(iteration: Int, def: LSystem): Pair<Float, Float> {
        val maxWidth = (1.0 / def.lineWidthExp.pow(iteration)) * def.lineWidthBold
        val minWidth = maxWidth / 10.0
        return Pair(minWidth.toFloat(), maxWidth.toFloat())
    }

    fun setLineWidthAccordingToImage(
        line: MutableList<LSTriple>,
        luminanceData: Array<FloatArray>,
        minWidth: Float,
        maxWidth: Float
    ) {
        val xScale = luminanceData.size - 1
        val yScale = luminanceData[0].size - 1
        line.forEach { p ->
            // Use the inverted brightness as width of the line we drawSpline.
            val lum = luminanceData[(p.x * xScale).toInt()][(p.y * yScale).toInt()]
            p.w = minWidth + lum * (maxWidth - minWidth)
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
    ): List<LSTriple> {
        val list: MutableList<LSTriple> = mutableListOf()

        var x = 0.0F
        var y = 0.0F
        var angle: Float = (PI / 2).toFloat()

        val stack: Stack<LSTriple> = Stack()

        list.add(LSTriple(x, y, 1F))
        for (c in instructions) {
            when (c.toString()) {
                "-" -> angle -= systemAngle
                "+" -> angle += systemAngle
                "[" -> stack.push(LSTriple(x, y, 1F))
                "]" -> {
                    val p = stack.pop()
                    x = p.x
                    y = p.y
                }
                in forwardChars -> {
                    x += kotlin.math.sin(angle)
                    y += kotlin.math.cos(angle)
                    list.add(LSTriple(x, y, 1F))
                }
            }
        }
        return list
    }

    private fun scalePolyPointList(list: List<LSTriple>): MutableList<LSTriple> {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (p in list) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y

            if (p.x > maxX) maxX = p.x
            if (p.y > maxY) maxY = p.y
        }

        val scaleX = 1 / (maxX - minX)
        val scaleY = 1 / (maxY - minY)

        val scale = kotlin.math.min(scaleX, scaleY)

        val xSpace = maxX - minX
        val ySpace = maxY - minY
        val offsetX = if (xSpace < ySpace) (ySpace - xSpace) / 2.0F else 0.0F
        val offsetY = if (ySpace < xSpace) (xSpace - ySpace) / 2.0F else 0.0F

        val scaledList: MutableList<LSTriple> = mutableListOf()
        for (p in list) {
            scaledList.add(LSTriple((p.x - minX + offsetX) * scale, (p.y - minY + offsetY) * scale, 1F))
        }

        return scaledList
    }

    private fun smoothenCurvatureOfLine(list: MutableList<LSTriple>, intermediateSplines: Int): MutableList<LSTriple> {
        val smoothedList: MutableList<LSTriple> = mutableListOf()
        for (i in -1 until list.size) {
            val p01 = list[(i - 1).coerceAtLeast(0)]
            val p02 = list[i.coerceAtLeast(0)]
            val p03 = list[(i + 1).coerceAtMost(list.size - 1)]
            addSplineBetweenPoints(
                getMidPoint(p01, p02),
                p02,
                getMidPoint(p02, p03),
                smoothedList,
                intermediateSplines
            )
        }
        return smoothedList
    }

    fun smoothenWidthOfLine(list: MutableList<LSTriple>) {
        for (i in 0 until list.size - 1) {
            val p00 = list[(i - 3).coerceAtLeast(0)]
            val p01 = list[(i - 2).coerceAtLeast(0)]
            val p02 = list[(i - 1).coerceAtLeast(0)]
            val p03 = list[i]
            val p04 = list[(i + 1).coerceAtMost(list.size - 1)]
            val p05 = list[(i + 2).coerceAtMost(list.size - 1)]
            val p06 = list[(i + 3).coerceAtMost(list.size - 1)]
            p03.w = p00.w * 0.05f +
                    p01.w * 0.1f +
                    p02.w * 0.2f +
                    p03.w * 0.3f +
                    p04.w * 0.2f +
                    p05.w * 0.1f +
                    p06.w * 0.05f
        }
    }

    fun addSideBuffer(outputSideBuffer: Float, vwLine: List<LSTriple>) {
        vwLine.forEach { p ->
            p.x = outputSideBuffer + p.x * (1 - 2 * outputSideBuffer)
            p.y = outputSideBuffer + p.y * (1 - 2 * outputSideBuffer)
        }
    }

    private fun getMidPoint(p0: LSTriple, p1: LSTriple): LSTriple {
        return LSTriple((p0.x + p1.x) / 2.0F, (p0.y + p1.y) / 2.0F, 1F)
    }

    private fun addSplineBetweenPoints(
        pp1: LSTriple, pp2: LSTriple, pp3: LSTriple,
        outputList: MutableList<LSTriple>, intermediateSplines: Int
    ) {
        // Populate this from LSystem.systems
        val tIncrement = 1.0 / intermediateSplines

        var t = 0.0
        while (t < (1.0 - (tIncrement / 2.0))) {
            // The bezier square spline is calculated using this formula from wikipedia.
            // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

            // Calculate the Bezier (x, y) coordinate for this step.
            val x = ((1 - t).pow(2.0) * pp1.x) +
                    (2 * (1 - t) * t * pp2.x) +
                    (t.pow(2.0) * pp3.x)
            val y = ((1 - t).pow(2.0) * pp1.y) +
                    (2 * (1 - t) * t * pp2.y) +
                    (t.pow(2.0) * pp3.y)

            outputList.add(LSTriple(x.toFloat(), y.toFloat(), 1F))

            // Increment the t value used in the Bezier calculations above.
            t += tIncrement
        }
    }
}
