import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.mainBody
import se.kjellstrand.lsystem.LSystemGenerator
import se.kjellstrand.lsystem.LSystemRenderer
import se.kjellstrand.lsystem.model.LSystemDefinition
import se.kjellstrand.lsystem.model.LSystemDefinitionList
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

    val fixIteration = 4
    var listOfSystemsToRender = listOf("Moore")//,"TwinDragon") //, "SierpinskiCurve", "Hilbert", "Peano", "Moore", "Gosper", "Fudgeflake")

    readLSystemDefinitions()?.let { lSystems ->
        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = lSystems.map { it.name }
        }
        val imageNames = listOf("debug.jpg")
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

    val polygon = LSystemGenerator.generatePolygon(lSystemDefinition, iterations)

    var bufferedImage = LSystemRenderer.renderLSystem(polygon, brightnessImage, outputImageSize)

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

