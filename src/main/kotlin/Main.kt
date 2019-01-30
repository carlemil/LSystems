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
 *  ./gradlew run -PlsArgs="['-s SnowFlake', '-i 4', '-o 600', '-w 0.6', '-v 0.2', '-b ceb.jpg' ]"
 *
 */

fun main(args: Array<String>) = mainBody {
    println("Init")

    ArgParser(args).parseInto(::LSArgParser).run {
        val lSystem = readLSystemDefinitions(lsystem)

        println(lsystem + "  " + lSystem?.name)

        val hueImage = if (!hueImageName.isEmpty()) readImageFile(hueImageName) else null
        val lightnessImage = if (!brightnessImageName.isEmpty()) readImageFile(brightnessImageName) else null
        val fileName = lSystem?.name + "_" + iterations +
                (if (!hueImageName.isEmpty()) "_" + hueImageName.subSequence(0, hueImageName.lastIndexOf(".")) else "") +
                (if (!brightnessImageName.isEmpty()) "_" + brightnessImageName.subSequence(0, brightnessImageName.lastIndexOf(".")) else "") +
                "_scale_" + outputImageSize.toInt()

        val pngFileName = fileName + ".png"

        val coordList = computeLSystem(lSystem!!, iterations)

        val sidePadding = outputImageSize / 50

        val lineWidthScaling = Math.pow(lSystem?.scaling, -iterations.toDouble()) * 300

        val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, hueImage, lightnessImage, outputImageSize,
                sidePadding, lineWidth * lineWidthScaling, (outlineWidth / 4) * lineWidthScaling)

        writeImageToPngFile(bufferedImage, pngFileName)

        println("Done")
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

