package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSystemDefinition
import java.lang.Math.PI
import java.util.*
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 */
object LSystemGenerator {

    fun generatePolygon(lSystemDefinition: LSystemDefinition, iterations: Int): List<Point> {
        var instructions =
            generate(lSystemDefinition.axiom, lSystemDefinition.rules, iterations, lSystemDefinition.forwardChars)

        val xyList = convertToPolyPointList(
            instructions.toString(),
            lSystemDefinition.getAngleInRadians(),
            lSystemDefinition.forwardChars
        )

        val scaledList = scalePolyPointList(xyList)

        return smoothenTheLine(scaledList)
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
        systemAngle: Double,
        forwardChars: Set<String>
    ): List<Point> {
        val list: MutableList<Point> = mutableListOf()

        var x = 0.0
        var y = 0.0
        var angle: Double = PI / 2

        val stack: Stack<Pair<Double, Double>> = Stack()

        list.add(Point(x, y))
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
                    list.add(Point(x, y))
                }
            }
        }
        return list
    }

    private fun scalePolyPointList(list: List<Point>): MutableList<Point> {
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

        val scaledList: MutableList<Point> = mutableListOf()
        for (p in list) {
            scaledList.add(Point((p.x - minX + offsetX) * scale, (p.y - minY + offsetY) * scale))
        }

        return scaledList
    }

    private fun smoothenTheLine(list: List<Point>): List<Point> {
        val smoothedList: MutableList<Point> = mutableListOf()
        for (i in -1 until list.size) {
            val p01 = list[(i - 1).coerceAtLeast(0)]
            val p02 = list[i.coerceAtLeast(0)]
            val p03 = list[(i + 1).coerceAtMost(list.size - 1)]
            addSplineBetweenPoints(
                Point.getMidPoint(p01, p02),
                p02,
                Point.getMidPoint(p02, p03),
                smoothedList
            )
        }
        return smoothedList
    }

    private fun addSplineBetweenPoints(
        pp1: Point, pp2: Point, pp3: Point,
        outputList: MutableList<Point>
    ) {
        val tincrement = 1.0 / 5.0
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

            outputList.add(Point(x, y))

            // Calculate the t value used in the Bezier calculations above.
            // Dont change the / X value here without updating in lSystem.polygon.VariableWidthPolygon.calculatePerpendicularPolyPoint()
            t += tincrement
        }
    }

}