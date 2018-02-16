import java.awt.image.BufferedImage
import java.io.File
import java.lang.Math.PI
import javax.imageio.ImageIO
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 */

fun main(args: Array<String>) {
    println("Start")
    val system = hilbertLSystem()
    println("Generate fractal")
    val steps = 8
    val coordList = computeLSystem(system, steps)

    println("Read image file")
    val image = readImageFile("arthur.png")

    val scale = 4000.0
    val sidePadding = scale / 50
    val strokeWidth: Double = 1500.0 * (1.0 / 2.0.pow(steps)) // 2^steps

    println("Write to file")
    writeCoordListToSVGFile(scale, sidePadding, coordList, system.getName() + "_" + steps,
            false, false, strokeWidth, image)
    println("Done")
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

private fun writeCoordListToSVGFile(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>,
                                    name: String, useBezierCurves: Boolean, wrapWithHtml: Boolean,
                                    strokeWidth: Double, image: BufferedImage) {
    var stringBuffer = StringBuffer()
    if (wrapWithHtml) {
        stringBuffer.append("<!DOCTYPE html>\n<html>\n<body>\n")
    }
    stringBuffer.append(
            "<svg width=\"" + (sidePadding * 2 + scale) + "\" height=\"" + (sidePadding * 2 + scale) + "\">")
    if (useBezierCurves) {
        val separator = " "
        for (i in 1..xyList.size - 2) {
            stringBuffer.append("\n<path d=\"")
            val p0 = xyList.get(i - 1)
            val p1 = xyList.get(i)
            val p2 = xyList.get(i + 1)
            stringBuffer.append("M " + getCoord(getCenter(p0, p1), scale, sidePadding, separator) +
                    " Q " + getCoord(p1, scale, sidePadding, separator) +
                    " " + getCoord(getCenter(p1, p2), scale, sidePadding, separator) + ", ")
            stringBuffer.append("\" stroke=\"#" + getColor(p1, image) + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    } else {
        for (i in 0..xyList.size - 2) {
            val p0 = xyList.get(i)
            val p1 = xyList.get(i + 1)
            stringBuffer.append("\n<polyline points=\"")
            stringBuffer.append(getCoord(p0, scale, sidePadding, ","))
            stringBuffer.append(" ")
            stringBuffer.append(getCoord(p1, scale, sidePadding, ","))
            stringBuffer.append("\" stroke=\"#" + getColor(p0, image) + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    }
    stringBuffer.append("</svg>\n")
    if (wrapWithHtml) {
        stringBuffer.append("</body>\n</html>\n")
    }
    fileWriter(stringBuffer.toString(), name +
            (if (useBezierCurves) "_bezier" else "") +
            (if (wrapWithHtml) ".html" else ".svg"))
}

fun getColor(p: Pair<Double, Double>, image: BufferedImage): String {
    val x = (p.first * (image.width - 1)).toInt()
    val y = (p.second * (image.height - 1)).toInt()
    //print("x: " + x + ", y: " + y + "  " + image.width.toString() + " - " + image.height.toString() + " P " + p + "\n")
    return (image.getRGB(x, y) and 0xffffff).toString(16)
    // return "ff00ff"
}

private fun fileWriter(text: String, name: String) {
    File(name).writeText(text)
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

class kochSnowFlakeLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return PI / 2.0
    }

    override fun getName(): String {
        return "KochSnowFlakeLSystem"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'F' to "F-F+F+FF-F-F+F",
                '+' to "+",
                '-' to "-")
    }

    override fun getAxiom(): String {
        return "F"
    }
}
