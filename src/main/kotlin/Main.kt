import LSystem.*
import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * Created by carlemil on 4/10/17.
 *
 *  ./gradlew run -PlsArgs="['-s SnowFlake', '-i 3', '-o 400', '-b che_b.png', '-u che_h.png' ]"
 *
 *  gradle run -PlsArgs="['-s b', '-i 4', '-o 800', '-b str.jpg', '-u str.jpg', '-B 1.5' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    if (args.size > 0) {
        ArgParser(args).parseInto(::LSArgParser).run {
            renderLSystem(lsystem, iterations, hueImageName, brightnessImageName, outputImageSize, lineWidth, bold)
        }
    } else {
        val lineWidthMod = 1.0
        for (iterations in 2..5) {
            renderLSystem("SnowFlake",
                    iterations,
                    "",
                    "str.jpg",
                    1400.0,
                    lineWidthMod,
                    0.0)
        }
    }
}

private fun renderLSystem(lsystem: String,
                          iterations: Int,
                          hueImageName: String,
                          brightnessImageName: String,
                          outputImageSize: Double,
                          lineWidthMod: Double,
                          boldWidth: Double) {

    val lSystem = readLSystemDefinitions(lsystem)

    val t0 = System.currentTimeMillis()

    println("Rendering " + lSystem?.name + ".")

    val hueImage = if (!hueImageName.isEmpty()) readImageFile(hueImageName) else null
    val lightnessImage = if (!brightnessImageName.isEmpty()) readImageFile(brightnessImageName) else null
    val fileName = lSystem?.name + "_" + iterations +
            (if (!hueImageName.isEmpty()) "_hue_" + hueImageName.subSequence(0, hueImageName.lastIndexOf(".")) else "") +
            (if (!brightnessImageName.isEmpty()) "_bri_" + brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")) else "") +
            "_scale_" + lSystem?.scaling +
            "_size_" + outputImageSize.toInt()

    val pngFileName = fileName + ".png"

    val coordList = computeLSystem(lSystem!!, iterations, boldWidth)

    val lineWidthScaling = (outputImageSize / Math.pow(lSystem.scaling, iterations.toDouble())) / 5.0

    val sidePadding = lineWidthScaling + outputImageSize / 20

    val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, hueImage, lightnessImage, outputImageSize,
            lSystem.lineWidth * lineWidthMod * lineWidthScaling, sidePadding)

    writeImageToPngFile(bufferedImage, pngFileName)

    val t1 = System.currentTimeMillis()

    println("Done after: " + (t1 - t0) + "ms\n")
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
    return lSystemInfo.systems.find { lsd -> lsd.name.startsWith(lSystemName, true) }
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

