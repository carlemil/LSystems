import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Math.PI
import javax.imageio.ImageIO
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 *
 * Conver output to png with image magic
 * C:\Program Files\ImageMagick-7.0.7-Q16>magick.exe -size 20000x20000 C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.svg C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.png
 */

fun main(args: Array<String>) {
    println("Init")
    val steps = 8
    val scale = 4000.0
    val sidePadding = scale / 50
    val strokeWidth: Double = 2500.0 * (1.0 / 2.0.pow(steps)) // 2^steps
    val useBezierCurves = false
    val colorRatio = 0.8
    val system = hilbertLSystem()
    val imageName = "cekj.png"
    val fileName = system.getName() + "_" + steps +
            (if (!imageName.isEmpty()) "_" + imageName.subSequence(0, imageName.lastIndexOf(".")) else "") +
            (if (useBezierCurves) "_bezier" else "")

    println("Generate fractal")
    val coordList = computeLSystem(system, steps)

    println("Read image file")
    val image = readImageFile(imageName)

    println("Get coord list as svg")
    val coordListSVG = getCoordListAsSVG(scale, sidePadding, coordList, system.getName() + "_" + steps,
            useBezierCurves, strokeWidth, image, colorRatio)

    println("Write SVG to file: " + fileName + ".svg")
    writeToFil(coordListSVG, fileName, ".svg")

    println("Write HTML to file: " + fileName + ".html")
    writeToFil(wrapWithHTML(coordListSVG), fileName, ".html")

    println("Done")
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

private fun getCoordListAsSVG(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>,
                              name: String, useBezierCurves: Boolean, strokeWidth: Double, image: BufferedImage,
                              colorRatio: Double): StringBuffer {
    var stringBuffer = StringBuffer()

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
            stringBuffer.append("\" stroke=\"#" + getColorFromImage(p1, image) + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    } else {
        for (i in 0..xyList.size - 2) {
            val p0 = xyList.get(i)
            val p1 = xyList.get(i + 1)
            val color = ColorUtils.getHexString(ColorUtils.blend(getColorFromImage(p0, image), getColorByIndex(i.toDouble()), colorRatio))
            stringBuffer.append("\n<polyline points=\"")
            stringBuffer.append(getCoord(p0, scale, sidePadding, ","))
            stringBuffer.append(" ")
            stringBuffer.append(getCoord(p1, scale, sidePadding, ","))
            stringBuffer.append("\" stroke=\"#" + color + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    }
    return stringBuffer
}

fun wrapWithHTML(stringBuffer: StringBuffer): StringBuffer {
    stringBuffer.append("<!DOCTYPE html>\n<html>\n<body>\n")
    stringBuffer.append("</body>\n</html>\n")
    return stringBuffer
}

fun writeToFil(stringBuffer: StringBuffer, fileName: String, fileEnding: String) {
    fileWriter(stringBuffer.toString(), fileName + fileEnding)
}

fun getColorFromImage(p: Pair<Double, Double>, image: BufferedImage): Color {
    val x = (p.first * (image.width - 1)).toInt()
    val y = (p.second * (image.height - 1)).toInt()
    val color = image.getRGB(x, y)
    return Color(color)// and 0xffffff).toString(16)
}

fun getColorByIndex(i: Double): Color {
    val a = 127
    val r = ((Math.sin(i / 701) + 1) * 127).toInt();
    val g = ((Math.sin(i / 71) + 1) * 127).toInt();
    val b = ((Math.sin(i / 7001) + 1) * 127).toInt();
    val color = Color(a, r, g, b)
    return color
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
