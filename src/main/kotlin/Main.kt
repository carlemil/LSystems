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
 *  TODO Args parser and docs are outdated, update some day.
 *
 *  ./gradlew run -PlsArgs="['-s SnowFlake', '-i 3', '-o 400', '-b che_b.png', '-u che_h.png' ]"
 *
 *  gradle run -PlsArgs="['-s b', '-i 4', '-o 800', '-b str.jpg', '-u str.jpg', '-B 1.5' ]"
 *
 */

fun main(args: Array<String>): Unit = mainBody {
    println("Init")

    if (args.isNotEmpty()) {
        ArgParser(args).parseInto(::LSArgParser).run {
            renderLSystem(
                    readLSystemDefinitions(lsystem),
                    iterations,
                    brightnessImageName,
                    null,
                    outputImageSize,
                    lineWidth,
                    bold)
        }
    } else {
        for (imageName in listOf("che2.jpg", "che3.jpg", "che4.jpg")) {
            for (systemName in listOf("Hilbert", "Peano", "SnowFlake")) {
                readLSystemDefinitions(systemName)?.let { lSystem ->
                    for (i in (lSystem.maxIterations - 1)..lSystem.maxIterations) {
                        println("$imageName - $systemName - $i")
                        renderLSystem(lSystem, i, imageName, null, 1200.0)
                    }
                }
            }
        }
    }
}

fun renderLSystem(lSystem: LSystemDefinition?,
                  iterations: Int,
                  brightnessImageName: String?,
                  brightnessImage_: BufferedImage?,
                  outputImageSize: Double,
                  lineWidthMod: Double = 1.0,
                  boldWidth: Double = 0.0) {
    val t0 = System.currentTimeMillis()

    val brightnessImage = if (brightnessImageName?.isNotEmpty() == true) readImageFile(brightnessImageName) else brightnessImage_

    val fileName = lSystem?.name +
            "_" + iterations +
            "_" + getFirstPartOfImageName(brightnessImageName) +
            "_scale_" + lSystem?.scaling +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "$fileName.png"

    val coordList = computeLSystem(lSystem!!, iterations, boldWidth)

    val lineWidthScaling = (outputImageSize / lSystem.scaling.pow(iterations.toDouble())) / 5.0

    val sidePadding = lineWidthScaling + outputImageSize / 20

    val (bufferedImage, g2) = setupGraphics(outputImageSize, sidePadding)

    val polygon = adjustWidthAccordingToImage(coordList, brightnessImage)

    val t1 = System.currentTimeMillis()
    println("Rendering " + lSystem.name + ": " + (t1 - t0) + "ms\n")

    VariableWidthPolygon.drawPolygonToBufferedImage(polygon, g2, outputImageSize,
            lSystem.lineWidth * lineWidthMod * lineWidthScaling, sidePadding)
    val t2 = System.currentTimeMillis()
    println("Render polygon in total: " + (t2 - t1) + "ms\n")

    writeImageToPngFile(bufferedImage, pngFileName)
    val t3 = System.currentTimeMillis()
    println("Write image to file: " + (t3 - t2) + "ms\n")

    val t4 = System.currentTimeMillis()
    println("Done after: " + (t4 - t3) + "ms\n")
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
        ppList.add(PolyPoint(p.x, p.y, p.w * c))
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

