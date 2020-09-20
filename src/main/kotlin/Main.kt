import lSystem.*
import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*
import kotlin.math.sin
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
            renderLSystem(readLSystemDefinitions(lsystem), iterations, hueImageName, brightnessImageName, outputImageSize, lineWidth, bold)
        }
    } else {
        readLSystemDefinitions("KochSnowFlake")?.let { lSystem ->
            //for (iterations in lSystem.maxIterations-3..lSystem.maxIterations) {
            renderLSystem(lSystem,
                    2, //iterations,
                    "",
                    "str.jpg",
                    400.0,
                    1.0,
                    0.0)
            //}
        }
    }
}

private fun renderLSystem(lSystem: LSystemDefinition?,
                          iterations: Int,
                          hueImageName: String,
                          brightnessImageName: String,
                          outputImageSize: Double,
                          lineWidthMod: Double,
                          boldWidth: Double) {
    val t0 = System.currentTimeMillis()

    println("Rendering " + lSystem?.name + ".")

    val hueImage = if (hueImageName.isNotEmpty()) readImageFile(hueImageName) else null
    val lightnessImage = if (brightnessImageName.isNotEmpty()) readImageFile(brightnessImageName) else null
    val fileName = lSystem?.name + "_" + iterations +
            (if (hueImageName.isNotEmpty()) "_hue_" + hueImageName.subSequence(0, hueImageName.lastIndexOf(".")) else "") +
            (if (brightnessImageName.isNotEmpty()) "_bri_" + brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")) else "") +
            "_scale_" + lSystem?.scaling +
            "_size_" + outputImageSize.toInt()

    val pngFileName = "$fileName.png"

    val coordList = computeLSystem(lSystem!!, iterations, boldWidth)

    val lineWidthScaling = (outputImageSize / lSystem.scaling.pow(iterations.toDouble())) / 5.0

    val sidePadding = lineWidthScaling + outputImageSize / 20

    val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, hueImage, lightnessImage, outputImageSize,
            lSystem.lineWidth * lineWidthMod * lineWidthScaling, sidePadding)

    writeImageToPngFile(bufferedImage, pngFileName)

    val t1 = System.currentTimeMillis()

    println("Done after: " + (t1 - t0) + "ms\n")
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

