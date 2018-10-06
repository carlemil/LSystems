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
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.FileOutputStream
import java.nio.file.Paths

/**
 * Created by carlemil on 4/10/17.
 *
 * gradle run -PlsArgs="['-v true', '-t black', '-s Hilbert', '-i 5', '-w 1.5' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    ArgParser(args).parseInto(::LSArgParser).run {

        val lSystem = readLSystemDefinitions(lsystem)

        println(lsystem + "  " + lSystem?.name)

        val image = if (!imageName.isEmpty()) readImageFile(imageName) else null
        val fileName = lSystem?.name + "_" + iterations +
                (if (!imageName.isEmpty()) "_" + imageName.subSequence(0, imageName.lastIndexOf(".")) else "") +
                "_" + themeName + (if (useBezierCurves) "_bezier" else "") + "_scale_" + outputImageSize.toInt()

        val svgFileName = fileName + ".svg"

        val palette = Palette.getPalette(Theme(themeName), Math.pow(4.0, 6.0).toInt(), 100)

        val coordList = computeLSystem(lSystem!!, iterations)

        println("Write SVG to file: " + svgFileName)
        File(svgFileName).delete()
        val svgBufferedWriter = File(svgFileName).bufferedWriter()
        svgBufferedWriter.append("")

        val c0 = coordList[1]
        val c1 = coordList[2]
        val strokeWidth: Double = Math.sqrt(Math.pow(c0.first - c1.first, 2.0) + Math.pow(c0.second - c1.second, 2.0)) * outputImageSize * lineWidth / 4.0
        val sidePadding = outputImageSize / 50 //strokeWidth * 2


        drawToBitmap(coordList, outputImageSize, sidePadding, palette)

//
//        writeSVGToFile(outputImageSize, sidePadding, coordList, useBezierCurves, useVariableLineWidth, strokeWidth, image, palette, svgBufferedWriter, paletteRepeat)
//
//        println("Write HTML wrapper file.")
//        writeSVGToHtmlFile(fileName + ".html", outputImageSize, sidePadding, coordList, useBezierCurves, useVariableLineWidth, strokeWidth, palette, image, paletteRepeat)
//
//        println("Write PNG file.")
//        convertSVGtoPNG(svgFileName, fileName + ".png")

        println("Done")
    }
}

private fun readLSystemDefinitions(lSystemName: String): LSystemDefinition? {
    val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())!!
    if (lSystemInfo.systems.isEmpty()) {
        println("Failed to read LSystem definitions.")
        System.exit(-1)
    }
    return lSystemInfo.systems.find { lsd -> lsd.name == lSystemName }
}

private fun drawToBitmap(coordList: List<Pair<Double, Double>>, size: Double, sidePadding: Double, palette: IntArray) {

    val d =  DrawLine.paint(coordList,size, sidePadding, palette)

    Thread.sleep(3000)
    println("asd: "+d)
}

private fun writeToPNG(bitmap: BufferedImage, file: File) {

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
    file.append("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\"\n" +
            " \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n" +
            "<svg version=\"1.0\" xmlns=\"http://www.w3.org/2000/svg\"\n ")
    file.append(" width=\"" + (sidePadding * 2 + scale) + "\" height=\"" + (sidePadding * 2 + scale) + "\">\n")
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

private fun convertSVGtoPNG(svgInFile: String, pngOutFile: String) {
    val svg_URI_input = Paths.get(svgInFile).toUri().toURL().toString()
    val input_svg_image = TranscoderInput(svg_URI_input)
    val png_ostream = FileOutputStream(pngOutFile)
    val output_png_image = TranscoderOutput(png_ostream)
    val my_converter = PNGTranscoder()
    my_converter.transcode(input_svg_image, output_png_image)
    png_ostream.flush()
    png_ostream.close()
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
