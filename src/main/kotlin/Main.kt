import LSystem.*
import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Created by carlemil on 4/10/17.
 *
 *  ./gradlew run -PlsArgs="['-s SnowFlake', '-i 3', '-o 400', '-b che_b.png', '-u che_h.png' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    ArgParser(args).parseInto(::LSArgParser).run {
        val lSystem = readLSystemDefinitions(lsystem)

        val t0 = System.currentTimeMillis()

        println("Rendering " + lSystem?.name + ".")

        val hueImage = if (!hueImageName.isEmpty()) readImageFile(hueImageName) else null
        val lightnessImage = if (!brightnessImageName.isEmpty()) readImageFile(brightnessImageName) else null
        val fileName = lSystem?.name + "_" + iterations +
                (if (!hueImageName.isEmpty()) "_" + hueImageName.subSequence(0, hueImageName.lastIndexOf(".")) else "") +
                (if (!brightnessImageName.isEmpty()) "_" + brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")) else "") +
                "_size_" + outputImageSize.toInt()

        val pngFileName = fileName + ".png"

        val coordList = computeLSystem(lSystem!!, iterations)

        val sidePadding = outputImageSize / 20

        val lineWidthScaling = (outputImageSize / Math.pow(2.0, lSystem.scaling * iterations.toDouble())) / 2

        val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, hueImage, lightnessImage, outputImageSize,
                sidePadding, lineWidth * lineWidthScaling, outlineWidth, debug)

        writeImageToPngFile(bufferedImage, pngFileName)

        val t1 = System.currentTimeMillis()

        println("Done after: " + (t1 - t0) + "ms\n")
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
    return lSystemInfo.systems.find { lsd -> lsd.name.startsWith(lSystemName, true) }
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

