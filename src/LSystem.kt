import java.lang.Math.*

/**
 * Created by carlemil on 4/10/17.
 */

fun computeLSystem(lSystem: LSystem, iterations: Int): List<Pair<Double, Double>> {

    val intructions = translate(lSystem.getAxiom(), lSystem.getRules(), iterations)

    val xyList = convertToXY(intructions, lSystem.getAngle())

    val scaleXYList = scaleXYList(xyList)

    return scaleXYList
}

private fun translate(axiom: String, rules: Map<Char, String>, iterations: Int): String {
    var tmp: String = axiom
    var next: String = ""
    for (i in 1..iterations) {
        next = ""
        for (c in tmp) {
            next += rules.get(c)
        }
        tmp = next
    }
    return next.replace("A", "").replace("B", "")
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