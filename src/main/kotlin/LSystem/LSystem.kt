package LSystem

import java.lang.Math.*
import java.util.*

/**
 * Created by carlemil on 4/10/17.
 */

fun computeLSystem(lSystem: LSystemDefinition, iterations: Int): List<Pair<Double, Double>> {
    val t0 = System.currentTimeMillis()
    val instructions = generate(lSystem.axiom, lSystem.rules, iterations, lSystem.forwardChars)
    val t1 = System.currentTimeMillis()
    print("Generated fractal in: " + (t1 - t0) + "ms\n")
    val xyList = convertToXY(instructions.toString(), lSystem.angle, lSystem.forwardChars)
    val t2 = System.currentTimeMillis()
    print("Convert to XY in: " + (t2 - t1) + "ms\n")
    val svg = scaleXYList(xyList)
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
        instructions.replace(Regex(c.toString()), "F")
    }
    return instructions
}

private fun convertToXY(instructions: String, systemAngle: Double, forwardChars: Set<String>): List<Pair<Double, Double>> {
    val list: MutableList<Pair<Double, Double>> = mutableListOf()

    var x = 0.0
    var y = 0.0
    var angle: Double = Math.PI / 2

    val stack: Stack<Pair<Double, Double>> = Stack()

    //list.add(Pair(x, y))
    // list.add(Pair(x, y))
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
            in forwardChars -> {
                x += sin(angle)
                y += cos(angle)
                list.add(Pair(x, y))
            }
        }
    }
    //list.add(Pair(x, y))
    return list
}

private fun scaleXYList(list: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
    var minX = Double.MAX_VALUE
    var maxX = Double.MIN_VALUE
    var minY = Double.MAX_VALUE
    var maxY = Double.MIN_VALUE

    for (p in list) {
        if (p.first < minX) minX = p.first
        if (p.second < minY) minY = p.second

        if (p.first > maxX) maxX = p.first
        if (p.second > maxY) maxY = p.second
    }

    val scaleX = 1 / (maxX - minX)
    val scaleY = 1 / (maxY - minY)

    var scaledList: MutableList<Pair<Double, Double>> = mutableListOf()
    for (p in list) {
        scaledList.add(Pair((p.first - minX) * scaleX, (p.second - minY) * scaleY))
    }

    return scaledList
}