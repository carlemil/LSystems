import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.mainBody
import se.kjellstrand.lsystem.ColorUtils
import se.kjellstrand.lsystem.LSystem
import se.kjellstrand.lsystem.model.LSystemDefinition
import se.kjellstrand.lsystem.model.LSystemDefinitionList
import se.kjellstrand.lsystem.polygon.PolygonPoint
import se.kjellstrand.lsystem.polygon.VariableWidthPolygon
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

/**
 * Created by carlemil on 4/10/17.
 *
 * $ magick montage -tile x7 *.png ./montage.jpg
 *
 */

fun main(args: Array<String>): Unit = mainBody {
    println("Init")
    val t0 = System.currentTimeMillis()

    val fixIteration = 0
    var listOfSystemsToRender = listOf("TwinDragon" ) //, "SierpinskiCurve", "Hilbert", "Peano", "Moore", "Gosper", "Fudgeflake")

    readLSystemDefinitions()?.let { lSystems ->
        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = lSystems.map { it.name }
        }
        val imageNames = listOf("str.jpg")
        val nbrOfImagesToRender = getNbrOfImagesToRender(listOfSystemsToRender, lSystems, imageNames.size, fixIteration)
        var imageNbr = 0.0

        for (imageName in imageNames) {
            val image = readImageFile("input/$imageName")
            for (systemName in listOfSystemsToRender) {
                getLSystemByName(systemName, lSystems)?.let { lSystem ->
                    val iterations = if (fixIteration > 0) {
                        fixIteration
                    } else {
                        lSystem.maxIterations
                    }
                    for (i in 1..iterations) {
                        val progress = ((++imageNbr / nbrOfImagesToRender) * 100).toInt()
                        println("----------- $imageName - $systemName - $i - $progress% ----------- ")
                        renderLSystem(lSystem, i, imageName, image, 600.0)
                    }
                }
            }
        }
    }

    val t1 = System.currentTimeMillis()
    println("Done after: " + (t1 - t0) + "ms\n")
}

//init

//renderSystemFromImage. image, systemName, iteration,

fun getNbrOfImagesToRender(listOfSystemsToRender: List<String>,
                           lSystems: List<LSystemDefinition>,
                           nbrOfInputImages: Int,
                           fixIteration: Int): Int {
    var totalIterations = 0
    for (systemName in listOfSystemsToRender) {
        if (fixIteration > 0) {
            totalIterations += fixIteration
        } else {
            totalIterations += getLSystemByName(systemName, lSystems)?.maxIterations ?: 0
        }
    }
    return totalIterations * nbrOfInputImages
}

fun renderLSystem(lSystemDefinition: LSystemDefinition,
                  iterations: Int,
                  brightnessImageName: String,
                  brightnessImage: BufferedImage,
                  outputImageSize: Double) {
    val t0 = System.currentTimeMillis()

    val fileName = getFirstPartOfImageName(brightnessImageName) +
            "_" + lSystemDefinition.name +
            "_iterations_" + iterations +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "output/$fileName.png"
    val lSystem = LSystem(lSystemDefinition,iterations)

    val polygon = lSystem.getPolygon()

    adjustWidthAccordingToImage(polygon, brightnessImage)

    val sidePadding = VariableWidthPolygon.calculateSidesOfTriangle(polygon[2], polygon[3]).third *
            outputImageSize / 5 + outputImageSize / 60

    val (bufferedImage, g2) = setupGraphics(outputImageSize, sidePadding)

    val t1 = System.currentTimeMillis()
    println("Rendering " + lSystemDefinition.name + ": " + (t1 - t0) + "ms\n")

    VariableWidthPolygon.drawPolygonToBufferedImage(polygon, g2, outputImageSize, sidePadding)
    // lSystem.polygon.VariableWidthPolygon.drawDebugPolygon(polygon, g2, outputImageSize, sidePadding)
    VariableWidthPolygon.tearDownGraphics(g2)

    val t2 = System.currentTimeMillis()
    println("Render polygon in total: " + (t2 - t1) + "ms\n")

    writeImageToPngFile(bufferedImage, pngFileName)
    val t3 = System.currentTimeMillis()
    println("Write image to file: " + (t3 - t2) + "ms\n")
}

fun readLSystemDefinitions(): List<LSystemDefinition>? {
    val lSystems = Klaxon().parse<LSystemDefinitionList>(File("src/main/resources/curves.json").readText())!!
    if (lSystems.systems.isEmpty()) {
        println("---------------------- Failed to read LSystem definitions ----------------------")
        exitProcess(-1)
    }
    return lSystems.systems
}

fun getLSystemByName(lSystemName: String, lSystems: List<LSystemDefinition>): LSystemDefinition? {
    return lSystems.find { lsd -> lsd.name.startsWith(lSystemName, true) }
}

private fun getFirstPartOfImageName(brightnessImageName: String?): String {
    return if (brightnessImageName?.isNotEmpty() == true)
        brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")).toString()
    else
        ""
}

private fun adjustWidthAccordingToImage(polygon: List<PolygonPoint>, image: BufferedImage?) {
    for (element in polygon) {
        // Use the inverted brightness as width of the line we drawSpline.
        element.w = (1 - ColorUtils.getBrightnessFromImage(element.x, element.y, image))
    }
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

