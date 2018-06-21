import LSystem.*
import LSystem.color.Palette
import LSystem.color.Theme
import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.BufferedWriter
import java.io.File
import javax.imageio.ImageIO

/**
 * Created by carlemil on 4/10/17.
 *
 * Conver output to png with image magic
 * C:\Program Files\ImageMagick-7.0.7-Q16>magick.exe -size 20000x20000 C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.svg C:\Users\CarlEmil\IdeaProjects\LSystem2.0\HilbertCurve_10.png
 *
 *
 * starting with 132 photos of my colleagues  in high res (5120x3413)
 *
 * for f in *.jpg; do magick convert "$f" -crop 3413x3413+853+0 +repage -scale 30% "$f"; done
 */
// for f in *.jpg; do mv -n "$f" "${f/*/$RANDOM.jpg}"; done
/**
 * magick montage -tile x12 -background #aaaaaa *.jpg ../montage.jpg
 *
 * magick HilbertCurve_11_asd.svg HilbertCurve_11_asd.svg.png
 *
 * [10:17][:~/source/LSystems(master)]$ gradle run -PlsArgs="['--verbose']"
 *
 * gradle run -PlsArgs="['-v true', '-t black', '-p wlm.jpg', '-s HilbertCurve', '-i 5', '-w 1.5' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    ArgParser(args).parseInto(::LSArgParser).run {

//        val system = when (lsystem) {
//            kochSnowFlakeLSystem().getName() -> kochSnowFlakeLSystem()
//            hilbertLSystem().getName() -> hilbertLSystem()
//            lineLSystem().getName() -> lineLSystem()
//            sierpinskiLSystem().getName() -> sierpinskiLSystem()
//            snowFlake1LSystem().getName() -> snowFlake1LSystem()
//            else -> dragonLSystem()
//        }


val system = readLSystem()

        val image = if (!imageName.isEmpty()) readImageFile(imageName) else null
        val fileName = system?.name + "_" + iterations +
                (if (!imageName.isEmpty()) "_" + imageName.subSequence(0, imageName.lastIndexOf(".")) else "") +
                "_" + themeName + (if (useBezierCurves) "_bezier" else "") + "_scale_" + outputImageSize.toInt() + ".svg"

        val palette = Palette.getPalette(Theme(themeName), Math.pow(4.0, 6.0).toInt(), 100)

        val coordList = computeLSystem(system!!, iterations)

        println("Write SVG to file: " + fileName)
        File(fileName).delete()
        val svgBufferedWriter = File(fileName).bufferedWriter()
        svgBufferedWriter.append("")

        val c0 = coordList[1]
        val c1 = coordList[2]
        val strokeWidth: Double = Math.sqrt(Math.pow(c0.first - c1.first, 2.0) + Math.pow(c0.second - c1.second, 2.0)) * outputImageSize * lineWidth / 4.0
        val sidePadding = outputImageSize / 50 //strokeWidth * 2

        writeSVGToFile(outputImageSize, sidePadding, coordList, useBezierCurves, useVariableLineWidth, strokeWidth, image, palette, svgBufferedWriter, paletteRepeat)

        val htmlFileName = fileName + ".html"
        println("Write HTML wrapper file: " + htmlFileName)
        writeSVGToHtmlFile(htmlFileName, outputImageSize, sidePadding, coordList, useBezierCurves, useVariableLineWidth, strokeWidth, palette, image, paletteRepeat)

        println("Done")
    }
}

private fun readLSystem(): LSystemDefinition? {
    val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())
    return lSystemInfo?.systems?.get(0)
}

private fun writeSVGToHtmlFile(htmlFileName: String, scale: Double, sidePadding: Double,
                               coordList: List<Pair<Double, Double>>, useBezierCurves: Boolean, useVariableLineWidth: Boolean,
                               strokeWidth: Double, palette: IntArray, image: BufferedImage?, paletteRepeat: Double) {
    File(htmlFileName).delete()
    val htmlBufferedWriter = File(htmlFileName).bufferedWriter()
    htmlBufferedWriter.append("<!DOCTYPE html>\n<html>\n<body>\n")
    writeSVGToFile(scale, sidePadding, coordList, useBezierCurves, useVariableLineWidth, strokeWidth, image, palette, htmlBufferedWriter, paletteRepeat)
    htmlBufferedWriter.append("</body>\n</html>\n")
    htmlBufferedWriter.flush()
}


private fun writeSVGToFile(scale: Double, sidePadding: Double, xyList: List<Pair<Double, Double>>,
                           useBezierCurves: Boolean, useVariableLineWidth: Boolean, strokeWidth: Double, image: BufferedImage?,
                           palette: IntArray, file: BufferedWriter, paletteRepeat: Double) {
    file.append("<svg width=\"" + (sidePadding * 2 + scale) + "\" height=\"" + (sidePadding * 2 + scale) + "\">\n")
    if (useBezierCurves) {
        val separator = " "
        for (i in 1..xyList.size - 2) {
            file.append("\n<path d=\"")
            val p0 = xyList.get(i - 1)
            val p1 = xyList.get(i)
            val p2 = xyList.get(i + 1)
            val brightness = getBrightnessFromImage(p0, image)
            val segmentStrokeWidth = getVariableLineWidth(useVariableLineWidth, strokeWidth, brightness)
            val color = getLineSegmentColor(useVariableLineWidth, i, brightness, palette, paletteRepeat, xyList)
            file.append("M " + getCoord(getCenter(p0, p1), scale, sidePadding, separator) +
                    " Q " + getCoord(p1, scale, sidePadding, separator) +
                    " " + getCoord(getCenter(p1, p2), scale, sidePadding, separator) + ", ")
            file.append("\" stroke=\"#" + color + "\" stroke-width=\"" + "%.2f".format(segmentStrokeWidth) + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    } else {
        for (i in 0..xyList.size - 2) {
            val p0 = xyList.get(i)
            val p1 = xyList.get(i + 1)
            val brightness = getBrightnessFromImage(p0, image)
            val segmentStrokeWidth = getVariableLineWidth(useVariableLineWidth, strokeWidth, brightness)
            val color = getLineSegmentColor(useVariableLineWidth, i, brightness, palette, paletteRepeat, xyList)
            file.append("\n<polyline points=\"")
            file.append(getCoord(p0, scale, sidePadding, ","))
            file.append(" ")
            file.append(getCoord(p1, scale, sidePadding, ","))
            file.append("\" stroke=\"#" + color + "\" stroke-width=\"" + "%.2f".format(segmentStrokeWidth) + "\" fill=\"none\" stroke-linecap=\"round\"/>\n")
        }
    }
    file.append("\n</svg>")
    file.flush()
}

private fun getLineSegmentColor(useVariableLineWidth: Boolean, i: Int, brightness: Double, palette: IntArray,
                                paletteRepeat: Double, xyList: List<Pair<Double, Double>>): String {
    return if (useVariableLineWidth) {
        ColorUtils.getHexString(getPaletteColorByLinePosition(i.toDouble() / xyList.size, brightness, palette, paletteRepeat))
    } else {
        "FF000000"
    }
}

private fun getVariableLineWidth(useVariableLineWidth: Boolean, strokeWidth: Double, brightness: Double): Double {
    return if (useVariableLineWidth) {
        strokeWidth * (0.2 + (1.0 - brightness))
    } else {
        strokeWidth
    }
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

fun getBrightnessFromImage(p: Pair<Double, Double>, image: BufferedImage?): Double {
    var color = 0xffffff
    if (image != null) {
        val x = (p.first * (image.width - 1)).toInt()
        val y = (p.second * (image.height - 1)).toInt()
        color = image.getRGB(x, y)
    }
    var c = FloatArray(3)
    Color.RGBtoHSB(
            color shr 16 and 255,
            color shr 8 and 255,
            color and 255,
            c)
    return c[2].toDouble()
}

fun getPaletteColorByLinePosition(linePosition: Double, brightness: Double, palette: IntArray, paletteRepeat: Double): Color {
    val a = 255
    val f3 = Palette.rgbToFloat3(palette[((linePosition * palette.size) * paletteRepeat).toInt() % palette.size])
    val r = (f3[2] * brightness).toInt()
    val g = (f3[1] * brightness).toInt()
    val b = (f3[0] * brightness).toInt()
    return Color(r, g, b, a)
}

private fun getCenter(p0: Pair<Double, Double>, p1: Pair<Double, Double>): Pair<Double, Double> {
    return Pair((p0.first + p1.first) / 2.0, (p0.second + p1.second) / 2.0)
}

private fun getCoord(p: Pair<Double, Double>, scale: Double, sidePadding: Double, separator: String): String {
    return "" + (p.first * scale + sidePadding).format(4) + separator + (p.second * scale + sidePadding).format(4)
}

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
