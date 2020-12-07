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

    var listOfSystemsToRender =
        listOf("Moore")//, "TwinDragon", "SierpinskiCurve", "Hilbert", "Peano", "Moore", "Gosper", "Fudgeflake")

    readLSystemDefinitions()?.let { lSystems ->
        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = lSystems.map { it.name }
        }
        val imageNames = listOf("debug.jpg")

        for (imageName in imageNames) {
            val image = readImageFile("input/$imageName")
            for (systemName in listOfSystemsToRender) {
                getLSystemByName(systemName, lSystems)?.let { lSystem ->
                    var i = 0
                    var result = true
                    while (result) {
                        i++
                        println("----------- $imageName - $systemName - $i ----------- ")
                        result = renderLSystem(lSystem, i, imageName, image, 1200.0)
                    }
                    println("Break at i = $i")
                }
            }
        }
    }

    val t1 = System.currentTimeMillis()
    println("Done after: " + (t1 - t0) + "ms\n")
}

fun renderLSystem(
    lSystemDefinition: LSystemDefinition,
    iterations: Int,
    brightnessImageName: String,
    brightnessImage: BufferedImage,
    outputImageSize: Double
): Boolean {
    val t0 = System.currentTimeMillis()

    val line = LSystemGenerator.generatePolygon(lSystemDefinition, iterations)
    val vwLine = line.map { linePoint -> LinePoint(linePoint.x, linePoint.y, 1.0) }

    val lineWidthExp = lSystemDefinition.lineWidthExp
    val widthBoldness = lSystemDefinition.lineWidthBold
    val maxWidth = (outputImageSize / (iterations + 1).toDouble().pow(lineWidthExp))*widthBoldness
    val minWidth = maxWidth / 10.0

    if (minWidth < 0.5) {
        return false
    }

    var bufferedImage = LSystemRenderer.renderLSystem(vwLine, brightnessImage, outputImageSize, minWidth, maxWidth)

    val fileName = getFirstPartOfImageName(brightnessImageName) +
            "_" + lSystemDefinition.name +
            "_iterations_" + iterations +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "output/$fileName.png"

    writeImageToPngFile(bufferedImage, pngFileName)
    val t1 = System.currentTimeMillis()
    println("Rendering and writing to file took: " + (t1 - t0) + "ms\n")
    return true
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

