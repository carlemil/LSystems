import java.io.File
import java.lang.Math.PI
import java.lang.Math.round

/**
 * Created by carlemil on 4/10/17.
 */

fun main(args: Array<String>) {
    println("Start")
    val system = snowFlake1LSystem()
    println("Generate fractal")
    val steps = 5
    val coordList = computeLSystem(system, steps)

    val scale = 3000.0
    val sidePadding = scale / 50
    val strokeWidth: Double = 2.0

    println("Write to file")
    writeCoordListToSVGFile(scale, sidePadding, coordList, system.getName() + "_" + steps, false, strokeWidth)
    writeCoordListToSVGFile(scale, sidePadding, coordList, system.getName() + "_" + steps, true, strokeWidth)
    println("Done")
}

private fun writeCoordListToSVGFile(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>,
                                    name: String, useBezierCurves: Boolean, strokeWidth: Double) {
    var stringBuffer: StringBuffer = StringBuffer()
    stringBuffer.append("<svg width=\"" + (sidePadding * 2 + scale) + "px\" height=\"" + (sidePadding * 2 + scale) + "px\">")
    if (useBezierCurves) {
        val separator: String = " "
        stringBuffer.append("\n<path d=\"")
        for (i in 1..xyList.size - 2) {
            val p0 = xyList.get(i - 1)
            val p1 = xyList.get(i)
            val p2 = xyList.get(i + 1)
            stringBuffer.append("M " + getCoord(getCenter(p0, p1), scale, sidePadding, separator) +
                    " Q " + getCoord(p1, scale, sidePadding, separator) +
                    " " + getCoord(getCenter(p1, p2), scale, sidePadding, separator) + ", ")
        }
        stringBuffer.append("\" stroke=\"#000000\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>")
    } else {
        stringBuffer.append("\n<polyline points=\"")
        for (p in xyList) {
            stringBuffer.append(getCoord(p, scale, sidePadding, ",") + " ")
        }
        stringBuffer.append("\" stroke=\"#000000\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>")
    }
    stringBuffer.append("\n</svg>")
    fileWriter(stringBuffer.toString(), name + (if (useBezierCurves) "_bezier" else ""))
}

private fun fileWriter(text: String, name: String) {
    File(name + ".svg").writeText(text)
}

private fun getCenter(p0: Pair<Double, Double>, p1: Pair<Double, Double>): Pair<Double, Double> {
    return Pair((p0.first + p1.first) / 2.0, (p0.second + p1.second) / 2.0)
}

private fun getCoord(p: Pair<Double, Double>, scale: Double, sidePadding: Double, separator: String): String {
    return "" + (p.first * scale + sidePadding).format(4) + separator + (p.second * scale + sidePadding).format(4)
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

class sierpinskiLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return PI / 3
    }

    override fun getName(): String {
        return "SierpinskiCurve"
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
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return PI / 2
    }

    override fun getName(): String {
        return "HilbertCurve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "-BF+AFA+FB-",
                'B' to "+AF-BFB-FA+",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "A"
    }
}

class dragonLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return PI / 2.0
    }

    override fun getName(): String {
        return "DragonCurve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "A+BF",
                'B' to "FA-B",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "FA"
    }
}

class snowFlake1LSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('A', 'B')
    }

    override fun getAngle(): Double {
        return PI / 3.0
    }

    override fun getName(): String {
        return "SnowFlake1Curve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "A-B--B+A++AA+B-",
                'B' to "+A-BB--B-A++A+B",
                '+' to "+",
                '-' to "-")
    }

    override fun getAxiom(): String {
        return "A"
    }
}
