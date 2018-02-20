package LSystem

import java.lang.Math.*

/**
 * Created by carlemil on 4/10/17.
 */

fun computeLSystem(lSystem: LSystem, iterations: Int): List<Pair<Double, Double>> {

    val t0 = System.currentTimeMillis()
    val instructions = translate(lSystem.getAxiom(), lSystem.getRules(), iterations, lSystem.getForwardChars())
    val t1 = System.currentTimeMillis()

    print("Generated fractal in: " + (t1 - t0) + "ms\n")

    val xyList = convertToXY(instructions.toString(), lSystem.getAngle())

    return scaleXYList(xyList)
}

private fun translate(axiom: String, rules: Map<Char, String>, iterations: Int, forwardChars: Set<Char>): StringBuilder {
    var tmp = StringBuilder()
    tmp.append(axiom)
    var instructions = StringBuilder()
    for (i in 1..iterations) {
        instructions.setLength(0)
        for (c in tmp) {
            instructions.append(rules.get(c))
        }
        tmp.setLength(0)
        tmp.append(instructions)
    }
    for (c in forwardChars) {
        instructions.replace(Regex(c.toString()), "F")
    }
    return instructions
}

private fun convertToXY(intructions: String, systemAngle: Double): List<Pair<Double, Double>> {
    val list: MutableList<Pair<Double, Double>> = mutableListOf()

    var x: Double = 0.0
    var y: Double = 0.0
    var angle: Double = 0.0

    list.add(Pair(x, y))
    list.add(Pair(x, y))
    for (c in intructions) {
        when (c) {
            '-' -> angle -= systemAngle
            '+' -> angle += systemAngle
            'F' -> {
                x += sin(angle)
                y += cos(angle)
                list.add(Pair(x, y))
            }
        }
    }
    list.add(Pair(x, y))
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