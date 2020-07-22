package lSystem

import java.lang.Math.*
import java.util.*

/**
 * Created by carlemil on 4/10/17.
 */

fun computeLSystem(lSystem: LSystemDefinition, iterations: Int, bold: Double): List<PolyPoint> {
    val t0 = System.currentTimeMillis()
    val instructions = generate(lSystem.axiom, lSystem.rules, iterations, lSystem.forwardChars)
    val t1 = System.currentTimeMillis()
    print("Generated fractal in: " + (t1 - t0) + "ms\n")
    val xyList = convertToPolyPointList(instructions.toString(), lSystem.angle, lSystem.forwardChars, bold)
    val t2 = System.currentTimeMillis()
    print("Convert to XY in: " + (t2 - t1) + "ms\n")
    val svg = scalePolyPointList(xyList)
    val t3 = System.currentTimeMillis()
    print("Scale XY list in: " + (t3 - t2) + "ms\n")
    return svg
}

private fun generate(axiom: String, rules: Map<String, String>, iterations: Int, forwardChars: Set<String>): StringBuilder {
    var tmp = StringBuilder()
    tmp.append(axiom)
    var instructions = StringBuilder()
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

private fun convertToPolyPointList(instructions: String, systemAngle: Double, forwardChars: Set<String>, bold: Double): List<PolyPoint> {
    val list: MutableList<PolyPoint> = mutableListOf()

    var x = 0.0
    var y = 0.0
    var angle: Double = PI / 2
    var width = 1.0

    val stack: Stack<Pair<Double, Double>> = Stack()

    list.add(PolyPoint(x, y, width))
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
            "w" -> width = width / bold
            "W" -> width = width * bold
            in forwardChars -> {
                x += sin(angle)
                y += cos(angle)
                list.add(PolyPoint(x, y, width))
            }
        }
    }
    return list
}

private fun scalePolyPointList(list: List<PolyPoint>): List<PolyPoint> {
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

    var scaledList: MutableList<PolyPoint> = mutableListOf()
    for (p in list) {
        scaledList.add(PolyPoint((p.x - minX) * scaleX, (p.y - minY) * scaleY, p.w))
    }

    return scaledList
}