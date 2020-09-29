import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import lSystem.LSystemDefinition
import lSystem.LSystemInfo
import lSystem.PolyPoint
import lSystem.computeLSystem
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.system.exitProcess

/**
 * Created by carlemil on 4/10/17.
 *
 * >magick montage -tile x8 -background #aaaaaa *.jpg ../montage.jpg
 *
 *
Moore curve
Alphabet: L, R
Constants: F, +, −
Axiom: LFL+F+LFL
Production rules:
L → −RF+LFL+FR−
R → +LF−RFR−FL+

Twindragon

It can be also written as a Lindenmayer system – it only needs adding another section in initial string:

angle 90°
initial string FX+FX+
string rewriting rules
X ↦ X+YF
Y ↦ FX−Y.

Twindragon curve.

angle 120°
initial string F
string rewriting rules
F ↦ F+F−F.

 */

fun main(args: Array<String>): Unit = mainBody {
    println("Init")
    val t0 = System.currentTimeMillis()

    for (imageName in listOf("che2.jpg", "che3.jpg", "che4.jpg")) {
        val image = readImageFile("input/$imageName")
        for (systemName in listOf("Peano", "Hilbert", "SnowFlake")) {
            readLSystemDefinitions(systemName)?.let { lSystem ->
                for (i in 1..lSystem.maxIterations) {
                    println("----------- $imageName - $systemName - $i ----------- ")
                    renderLSystem(lSystem, i, imageName, image, 12600.0)
                }
            }
        }
    }

    val t1 = System.currentTimeMillis()
    println("Done after: " + (t1 - t0) + "ms\n")
}

fun renderLSystem(lSystem: LSystemDefinition?,
                  iterations: Int,
                  brightnessImageName: String,
                  brightnessImage: BufferedImage,
                  outputImageSize: Double,
                  boldWidth: Double = 1.0) {
    val t0 = System.currentTimeMillis()

    val fileName =
            "_" + getFirstPartOfImageName(brightnessImageName) +
                    "_" + lSystem?.name +
                    "_scale_" + lSystem?.scaling +
                    "_" + iterations +
                    "_size_" + outputImageSize.toInt()

    val pngFileName = "output/$fileName.png"

    val coordList = computeLSystem(lSystem!!, iterations, boldWidth)

    val polygon = adjustWidthAccordingToImage(coordList, brightnessImage)

    val sidePadding = VariableWidthPolygon.calculateSidesOfTriangle(polygon[0], polygon[1]).third *
            outputImageSize / 2 + outputImageSize / 60

    val (bufferedImage, g2) = setupGraphics(outputImageSize, sidePadding)

    val t1 = System.currentTimeMillis()
    println("Rendering " + lSystem.name + ": " + (t1 - t0) + "ms\n")

    VariableWidthPolygon.drawPolygonToBufferedImage(polygon, g2, outputImageSize, sidePadding)
    val t2 = System.currentTimeMillis()
    println("Render polygon in total: " + (t2 - t1) + "ms\n")

    writeImageToPngFile(bufferedImage, pngFileName)
    val t3 = System.currentTimeMillis()
    println("Write image to file: " + (t3 - t2) + "ms\n")
}

fun readLSystemDefinitions(lSystemName: String): LSystemDefinition? {
    val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())!!
    if (lSystemInfo.systems.isEmpty()) {
        println("Failed to read LSystem definitions.")
        exitProcess(-1)
    }
    return lSystemInfo.systems.find { lsd -> lsd.name.startsWith(lSystemName, true) }
}

private fun getFirstPartOfImageName(brightnessImageName: String?): String {
    return if (brightnessImageName?.isNotEmpty() == true)
        brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")).toString()
    else
        ""
}

private fun adjustWidthAccordingToImage(polygon: List<PolyPoint>, image: BufferedImage?): List<PolyPoint> {
    val ppList = ArrayList<PolyPoint>()
    for (element in polygon) {
        var p = element
        // Use the inverted brightness as width of the line we drawSpline.
        val c = (1 - ColorUtils.getBrightnessFromImage(p.x, p.y, image))
        ppList.add(PolyPoint(p.x, p.y, c))
    }
    return ppList
}

private fun setupGraphics(size: Double, sidePadding: Double): Pair<BufferedImage, Graphics2D> {
    val bufferedImage = BufferedImage((size + sidePadding * 2).toInt(), (size + sidePadding * 2).toInt(),
            BufferedImage.TYPE_INT_RGB)

    val g2 = bufferedImage.createGraphics()
    val rh = mutableMapOf<RenderingHints.Key, Any>()
    rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
    rh[RenderingHints.KEY_ALPHA_INTERPOLATION] = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
    rh[RenderingHints.KEY_COLOR_RENDERING] = RenderingHints.VALUE_COLOR_RENDER_QUALITY
    rh[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
    rh[RenderingHints.KEY_STROKE_CONTROL] = RenderingHints.VALUE_STROKE_PURE
    g2.setRenderingHints(rh)

    g2.stroke = BasicStroke(2f)
    g2.color = Color.WHITE
    g2.fill(Rectangle(0, 0, bufferedImage.width, bufferedImage.height))
    return Pair(bufferedImage, g2)
}

private fun writeImageToPngFile(bufferedImage: BufferedImage, pngFileName: String) {
    val file = File(pngFileName)
    ImageIO.write(bufferedImage, "png", file)
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

