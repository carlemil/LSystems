package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystemDefinition
import se.kjellstrand.variablewidthpolygon.PolygonPoint
import java.lang.Math.*
import java.util.*
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 */
object LSystemGenerator {

    fun generatePolygon(lSystemDefinition: LSystemDefinition, maxIterations: Int): List<PolygonPoint> {
        val t0 = System.currentTimeMillis()

        val iterations = 1.coerceAtLeast(lSystemDefinition.maxIterations.coerceAtMost(maxIterations))
        var instructions = generate(lSystemDefinition.axiom, lSystemDefinition.rules, iterations, lSystemDefinition.forwardChars)

        val t1 = System.currentTimeMillis()
        print("Generated instructions in: " + (t1 - t0) + "ms\n")

        val xyList = convertToPolyPointList(instructions.toString(), lSystemDefinition.getAngleInRadians(), lSystemDefinition.forwardChars)

        val t2 = System.currentTimeMillis()
        print("Convert to XY in: " + (t2 - t1) + "ms\n")

        val scaledList = scalePolyPointList(xyList)

        val t3 = System.currentTimeMillis()
        print("Scale XY list in: " + (t3 - t2) + "ms\n")

        val smoothenList = smoothenTheLine(scaledList)

        val t4 = System.currentTimeMillis()
        print("Smoothen list in: " + (t4 - t3) + "ms\n")

        return smoothenList
    }

    private fun generate(axiom: String, rules: Map<String, String>, iterations: Int, forwardChars: Set<String>): StringBuilder {
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

    private fun convertToPolyPointList(instructions: String, systemAngle: Double, forwardChars: Set<String>): List<PolygonPoint> {
        val list: MutableList<PolygonPoint> = mutableListOf()

        var x = 0.0
        var y = 0.0
        var angle: Double = PI / 2
        var width = 1.0

        val stack: Stack<Pair<Double, Double>> = Stack()

        list.add(PolygonPoint(x, y, width))
        for (c in instructions) {
            when (c.toString()) {
                "-" -> angle -= systemAngle
                "+" -> angle += systemAngle
                "[" -> stack.push(Pair(x, y))
                "]" -> {
                    val p = stack.pop()
                    x = p.first
                    y = p.second
                    // Start a new list in list here to denote a new polyline
                }
//                    "w" -> width = width / bold
//                    "W" -> width = width * bold
                in forwardChars -> {
                    x += sin(angle)
                    y += cos(angle)
                    list.add(PolygonPoint(x, y, width))
                }
            }
        }
        return list
    }

    private fun scalePolyPointList(list: List<PolygonPoint>): MutableList<PolygonPoint> {
        var minX = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var minY = Double.MAX_VALUE
        var maxY = Double.MIN_VALUE

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
        val offsetX = if (xSpace < ySpace) (ySpace - xSpace) / 2.0 else 0.0
        val offsetY = if (ySpace < xSpace) (xSpace - ySpace) / 2.0 else 0.0

        val scaledList: MutableList<PolygonPoint> = mutableListOf()
        for (p in list) {
            scaledList.add(PolygonPoint((p.x - minX + offsetX) * scale, (p.y - minY + offsetY) * scale, p.w))
        }

        return scaledList
    }

    private fun smoothenTheLine(list: List<PolygonPoint>): List<PolygonPoint> {
        val smoothedList: MutableList<PolygonPoint> = mutableListOf()
        for (i in -1 until list.size) {
            val p01 = list[(i - 1).coerceAtLeast(0)]
            val p02 = list[i.coerceAtLeast(0)]
            val p03 = list[(i + 1).coerceAtMost(list.size - 1)]
            addSplineBetweenPoints(
                    PolygonPoint.getMidPoint(p01, p02),
                    p02,
                    PolygonPoint.getMidPoint(p02, p03),
                    smoothedList)
        }
        return smoothedList
    }

    private fun addSplineBetweenPoints(pp1: PolygonPoint, pp2: PolygonPoint, pp3: PolygonPoint,
                                       outputList: MutableList<PolygonPoint>) {
        val tincrement = 1.0 / 3.0
        var t = 0.0
        while (t < (1.0 - (tincrement / 2.0))) {
            // The bezier square spline is calculated using this formula from wikipedia.
            // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

            // Calculate the Bezier (x, y) coordinate for this step.
            val x = ((1 - t).pow(2.0) * pp1.x) +
                    (2 * (1 - t) * t * pp2.x) +
                    (t.pow(2.0) * pp3.x)
            val y = ((1 - t).pow(2.0) * pp1.y) +
                    (2 * (1 - t) * t * pp2.y) +
                    (t.pow(2.0) * pp3.y)

            outputList.add(PolygonPoint(x, y))

            // Calculate the t value used in the Bezier calculations above.
            // Dont change the / X value here without updating in lSystem.polygon.VariableWidthPolygon.calculatePerpendicularPolyPoint()
            t += tincrement
        }
    }

}