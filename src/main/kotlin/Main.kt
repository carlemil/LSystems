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
            renderLSystem(readLSystemDefinitions(lsystem), iterations, brightnessImageName, outputImageSize, lineWidth, bold)
        }
    } else {
        readLSystemDefinitions("Hilbert")?.let { lSystem ->
            //for (iterations in lSystem.maxIterations-3..lSystem.maxIterations) {
            renderLSystem(lSystem,
                    5,
                    "str.jpg",
                    800.0,
                    1.0,
                    0.0)
            //}
        }
    }
}

private fun renderLSystem(lSystem: LSystemDefinition?,
                          iterations: Int,
                          brightnessImageName: String,
                          outputImageSize: Double,
                          lineWidthMod: Double,
                          boldWidth: Double) {
    val t0 = System.currentTimeMillis()

    println("Rendering " + lSystem?.name + ".")

    val brightnessImage = if (brightnessImageName.isNotEmpty()) readImageFile(brightnessImageName) else null
    val fileName = lSystem?.name + "_" + iterations +
            (if (brightnessImageName.isNotEmpty()) "_bri_" + brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")) else "") +
            "_scale_" + lSystem?.scaling +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "$fileName.png"

    val coordList = computeLSystem(lSystem!!, iterations, boldWidth)

    val lineWidthScaling = (outputImageSize / lSystem.scaling.pow(iterations.toDouble())) / 5.0

    val sidePadding = lineWidthScaling + outputImageSize / 20

    val (bufferedImage, g2) = setupGraphics(outputImageSize, sidePadding)

    val polygon = adjustWidthAccordingToImage(coordList, brightnessImage)

    VariableWidthPolygon.drawPolygonToBufferedImage(polygon, g2, outputImageSize,
            lSystem.lineWidth * lineWidthMod * lineWidthScaling, sidePadding)

    val t1 = System.currentTimeMillis()
    println("Write image to file: " + (t1 - t0) + "ms\n")
    writeImageToPngFile(bufferedImage, pngFileName)

    val t2 = System.currentTimeMillis()
    println("Done after: " + (t2 - t1) + "ms\n")
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

private fun readLSystemDefinitions(lSystemName: String): LSystemDefinition? {
    val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())!!
    if (lSystemInfo.systems.isEmpty()) {
        println("Failed to read LSystem definitions.")
        exitProcess(-1)
    }
    return lSystemInfo.systems.find { lsd -> lsd.name.startsWith(lSystemName, true) }
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

