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
 *  gradle run -PlsArgs="['-v true', '-t black', '-l SnowFlake', '-i 5', '-s 1600', '-w 0.6', '-p ceb.jpg' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    ArgParser(args).parseInto(::LSArgParser).run {

        val lSystem = readLSystemDefinitions(lsystem)

        println(lsystem + "  " + lSystem?.name)

        val inputImage = if (!imageName.isEmpty()) readImageFile(imageName) else null
        val fileName = lSystem?.name + "_" + iterations +
                (if (!imageName.isEmpty()) "_" + imageName.subSequence(0, imageName.lastIndexOf(".")) else "") +
                "_" + themeName + (if (useBezierCurves) "_bezier" else "") + "_scale_" + outputImageSize.toInt()

        val pngFileName = fileName + ".png"

        val palette = Palette.getPalette(Theme(themeName), Math.pow(4.0, 6.0).toInt(), 100)

        val coordList = computeLSystem(lSystem!!, iterations)

        val sidePadding = outputImageSize / 50 //strokeWidth * 2

        val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, inputImage, outputImageSize, sidePadding, lineWidth, palette)

        writeImageToPngFile(bufferedImage, pngFileName)

        println("Done")
    }
}

private fun writeImageToPngFile(bufferedImage: java.awt.image.BufferedImage, pngFileName: String) {
    val file = File(pngFileName)
    ImageIO.write(bufferedImage, "png", file)
}

private fun readLSystemDefinitions(lSystemName: String): LSystemDefinition? {
    val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())!!
    if (lSystemInfo.systems.isEmpty()) {
        println("Failed to read LSystem definitions.")
        System.exit(-1)
    }
    return lSystemInfo.systems.find { lsd -> lsd.name == lSystemName }
}


private fun getLineSegmentColor(useVariableLineWidth: Boolean, i: Int, brightness: Double, palette: IntArray,
                                paletteRepeat: Double, xyList: List<Pair<Double, Double>>): String {
    return if (useVariableLineWidth) {
        ColorUtils.getHexString(getPaletteColorByLinePosition(i.toDouble() / xyList.size, brightness, palette, paletteRepeat))
    } else {
        "FF000000"
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
