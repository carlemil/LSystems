import LSystem.computeLSystem
import LSystem.hilbertLSystem
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.BufferedWriter
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow

/**
 * Created by carlemil on 4/10/17.
 *
 * Conver output to png with image magic
 * C:\Program Files\ImageMagick-7.0.7-Q16>magick.exe -size 20000x20000 C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.svg C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.png
 *
 *
 * starting with 132 photos of my collegues in high res (5120x3413)
 *
for f in *.jpg; do magick convert "$f" -crop 3413x3413+853+0 +repage -scale 30% "$f"; done

magick montage -tile x12 -background #aaaaaa *.jpg ../montage.jpg

magick HilbertCurve_11_asd.svg HilbertCurve_11_asd.svg.png

 */

fun main(args: Array<String>) {
    println("Init")
    val steps = 8
    val scale = 800.0
    val sidePadding = scale / 50
    val strokeWidth: Double = scale * (0.6 / 2.0.pow(steps)) // 2^steps
    val useBezierCurves = false
    val system = hilbertLSystem()
    val imageName = "mont.jpg" //https://www.fotojet.com https://ipiccy.com/
    val image = readImageFile(imageName)
    val fileName = system.getName() + "_" + steps +
            (if (!imageName.isEmpty()) "_" + imageName.subSequence(0, imageName.lastIndexOf(".")) else "") +
            (if (useBezierCurves) "_bezier" else "") + "_scale_" + scale.toInt() + ".svg"

    val coordList = computeLSystem(system, steps)

    println("Write SVG to file: " + fileName)
    File(fileName).delete()
    val svgBufferedWriter = File(fileName).bufferedWriter()
    svgBufferedWriter.append("")
    writeSVGToFile(scale, sidePadding, coordList, useBezierCurves, strokeWidth, image, svgBufferedWriter)

    if (steps < 10) {
        val htmlFileName = fileName + ".html"
        println("Write HTML wrapper file: " + htmlFileName)
        writeSVGToHtmlFile(htmlFileName, scale, sidePadding, coordList, useBezierCurves, strokeWidth, image)
    }
    println("Done")
}

private fun writeSVGToHtmlFile(htmlFileName: String, scale: Double, sidePadding: Double,
                               coordList: List<Pair<Double, Double>>, useBezierCurves: Boolean,
                               strokeWidth: Double, image: BufferedImage) {
    File(htmlFileName).delete()
    val htmlBufferedWriter = File(htmlFileName).bufferedWriter()
    htmlBufferedWriter.append("<!DOCTYPE html>\n<html>\n<body>\n")
    writeSVGToFile(scale, sidePadding, coordList, useBezierCurves, strokeWidth, image, htmlBufferedWriter)
    htmlBufferedWriter.append("</body>\n</html>\n")
    htmlBufferedWriter.flush()
}


private fun writeSVGToFile(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>,
                           useBezierCurves: Boolean, strokeWidth: Double, image: BufferedImage,
                           file: BufferedWriter) {
    file.append("<svg width=\"" + (sidePadding * 2 + scale) + "\" height=\"" + (sidePadding * 2 + scale) + "\">\n")
    if (useBezierCurves) {
        val separator = " "
        for (i in 1..xyList.size - 2) {
            file.append("\n<path d=\"")
            val p0 = xyList.get(i - 1)
            val p1 = xyList.get(i)
            val p2 = xyList.get(i + 1)
            file.append("M " + getCoord(getCenter(p0, p1), scale, sidePadding, separator) +
                    " Q " + getCoord(p1, scale, sidePadding, separator) +
                    " " + getCoord(getCenter(p1, p2), scale, sidePadding, separator) + ", ")
            file.append("\" stroke=\"#" + getColorFromImage(p1, image) + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    } else {
        for (i in 0..xyList.size - 2) {
            val p0 = xyList.get(i)
            val p1 = xyList.get(i + 1)
            val color = ColorUtils.getHexString(
                    getColorByLinePosition(i.toDouble() / xyList.size * Math.PI, getColorFromImage(p0, image).blue.toDouble()))
            file.append("\n<polyline points=\"")
            file.append(getCoord(p0, scale, sidePadding, ","))
            file.append(" ")
            file.append(getCoord(p1, scale, sidePadding, ","))
            file.append("\" stroke=\"#" + color + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    }
    file.append("\n</svg>")
    file.flush()
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

fun getColorFromImage(p: Pair<Double, Double>, image: BufferedImage): Color {
    val x = (p.first * (image.width - 1)).toInt()
    val y = (p.second * (image.height - 1)).toInt()
    val color = image.getRGB(x, y)
    return Color(color)
}

fun getColorByLinePosition(linePosition: Double, imagePressure: Double): Color {
    val a = imagePressure.toInt()
    val r = (getSinFactor(linePosition * 7F) * imagePressure).toInt()
    val g = (getSinFactor(linePosition * 13F) * imagePressure).toInt()
    val b = (getSinFactor(linePosition * 3F) * imagePressure).toInt()
    return Color(a, r, g, b)
}

private fun getSinFactor(value: Double) = (Math.sin(value) + 1.0) / 2.0

private fun getCenter(p0: Pair<Double, Double>, p1: Pair<Double, Double>): Pair<Double, Double> {
    return Pair((p0.first + p1.first) / 2.0, (p0.second + p1.second) / 2.0)
}

private fun getCoord(p: Pair<Double, Double>, scale: Double, sidePadding: Double, separator: String): String {
    return "" + (p.first * scale + sidePadding).format(4) + separator + (p.second * scale + sidePadding).format(4)
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
