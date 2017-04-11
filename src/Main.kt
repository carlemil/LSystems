import java.io.File
import java.lang.Math.round

/**
 * Created by carlemil on 4/10/17.
 */

fun main(args: Array<String>) {
    println("Start")
    val system = sierpinskiLSystem()
    println("Generate fractal")
    val xyList = computeLSystem(system, 12)

    val sidePadding = 4.0
    val scale = 3000.0
    println("Write to file")
    writeLSystemToSVGFile(scale, sidePadding, xyList, system.getName())
    println("Done")
}

private fun writeLSystemToSVGFile(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>, name: String) {
    var stringBuffer: StringBuffer = StringBuffer()
    stringBuffer.append("<svg width=\"" + (sidePadding * 2 + scale) + "px\" height=\"" + (sidePadding * 2 + scale) + "px\">\n<polyline points=\"")
    for (i in 1..xyList.size - 2) {
        stringBuffer.append(getCoord(xyList.get(i + 0), scale, sidePadding) + " ")
    }
    stringBuffer.append("\" stroke=\"#000000\" stroke-width=\"0.2\" fill=\"none\" stroke-linecap=\"round\"/>\n</svg>")
    fileWriter(stringBuffer.toString(), name)
}

private fun fileWriter(text: String, name: String) {
    File(name+".svg").writeText(text)
}

private fun getCoord(p: Pair<Double, Double>, scale: Double, sidePadding: Double): String {
    return "" + (p.first * scale + sidePadding).format(4) + "," + (p.second * scale + sidePadding).format(4)
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

class sierpinskiLSystem : LSystem {
    override fun getName(): String {
        return "Sierpinski"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "BF-AF-B",
                'B' to "AF+BF+A",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "A"
    }
}

class hilbertLSystem : LSystem {
    override fun getName(): String {
        return "Sierpinski"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "BF-AF-B",
                'B' to "AF+BF+A",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "A"
    }
}
