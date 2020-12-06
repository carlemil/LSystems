import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.mainBody
import se.kjellstrand.lsystem.LSystemGenerator
import se.kjellstrand.lsystem.LSystemRenderer
import se.kjellstrand.lsystem.model.LSystemDefinition
import se.kjellstrand.lsystem.model.LSystemDefinitionList
import se.kjellstrand.variablewidthline.LinePoint
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.system.exitProcess

/**
 * Created by carlemil on 4/10/17.
 *
 * $ magick montage -tile x7 *.png ./montage.jpg
 *
 */

fun main(): Unit = mainBody {
    println("Init")
    val t0 = System.currentTimeMillis()

    val fixIteration = 0
    var listOfSystemsToRender =
        listOf("Fudgeflake")//, "TwinDragon") //, "SierpinskiCurve", "Hilbert", "Peano", "Moore", "Gosper", "Fudgeflake")

    readLSystemDefinitions()?.let { lSystems ->
        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = lSystems.map { it.name }
        }
        val imageNames = listOf("che4.jpg")

        for (imageName in imageNames) {
            val image = readImageFile("input/$imageName")
            for (systemName in listOfSystemsToRender) {
                getLSystemByName(systemName, lSystems)?.let { lSystem ->
                    val iterations = if (fixIteration > 0) {
                        fixIteration..fixIteration
                    } else {
                        1..lSystem.maxIterations
                    }
                    for (i in iterations) {
                        println("----------- $imageName - $systemName - $i ----------- ")
                        renderLSystem(lSystem, i, imageName, image, 600.0)
                    }
                }
            }
        }
    }

    val t1 = System.currentTimeMillis()
    println("Done after: " + (t1 - t0) + "ms\n")
}

fun getNbrOfImagesToRender(
    listOfSystemsToRender: List<String>,
    lSystems: List<LSystemDefinition>,
    nbrOfInputImages: Int,
    fixIteration: Int
): Int {
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

fun renderLSystem(
    lSystemDefinition: LSystemDefinition,
    iterations: Int,
    brightnessImageName: String,
    brightnessImage: BufferedImage,
    outputImageSize: Double
) {

    val line = LSystemGenerator.generatePolygon(lSystemDefinition, iterations)
    val vwLine = line.map { linePoint -> LinePoint(linePoint.x, linePoint.y, 1.0) }

    val minMinWidth = 0.5
    val maxWidth = minMinWidth + outputImageSize / 2.0.pow(iterations.toDouble()) / 3.0
    val minWidth = minMinWidth + maxWidth / 10.0

    var bufferedImage = LSystemRenderer.renderLSystem(vwLine, brightnessImage, outputImageSize, minWidth, maxWidth)

    val t0 = System.currentTimeMillis()


    val fileName = getFirstPartOfImageName(brightnessImageName) +
            "_" + lSystemDefinition.name +
            "_iterations_" + iterations +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "output/$fileName.png"

    writeImageToPngFile(bufferedImage, pngFileName)
    val t1 = System.currentTimeMillis()
    println("Write image to file: " + (t1 - t0) + "ms\n")
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

private fun writeImageToPngFile(bufferedImage: BufferedImage, pngFileName: String) {
    val file = File(pngFileName)
    ImageIO.write(bufferedImage, "png", file)
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

