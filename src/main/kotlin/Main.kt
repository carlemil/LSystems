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
        val lightnessImage = if (!lightnessImageName.isEmpty()) readImageFile(lightnessImageName) else null
        val fileName = lSystem?.name + "_" + iterations +
                (if (!hueImageName.isEmpty()) "_" + hueImageName.subSequence(0, hueImageName.lastIndexOf(".")) else "") +
                (if (!lightnessImageName.isEmpty()) "_" + lightnessImageName.subSequence(0, lightnessImageName.lastIndexOf(".")) else "") +
//                "_" + themeName +
                "_scale_" + outputImageSize.toInt()

        val pngFileName = fileName + ".png"

//        val palette = Palette.getPalette(Theme(themeName), Math.pow(4.0, 6.0).toInt(), 100)

        val coordList = computeLSystem(lSystem!!, iterations)

        val sidePadding = outputImageSize / 50 //strokeWidth * 2

        val bufferedImage = SplineLines.drawPolygonAsSplines(coordList, hueImage, lightnessImage,
                outputImageSize, sidePadding, lineWidth, outlineWidth)

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
    return lSystemInfo.systems.find { lsd -> lsd.name == lSystemName }
}

private fun readImageFile(file: String): BufferedImage {
    return ImageIO.read(File(file))
}

