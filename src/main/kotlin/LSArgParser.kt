import LSystem.*
import com.beust.klaxon.Klaxon
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class LSArgParser(parser: ArgParser) {

    val iterations by parser.storing(
            "-i", "--iterations",
            help = "Number of iterations of the L system") { trim().toInt() }.default(4)

    val outputImageSize by parser.storing(
            "-o", "--outputSize",
            help = "Size of the output svg image (in pixels)") { trim().toDouble() }.default(800.0)

    val hueImageName by parser.storing(
            "-u", "--hue",
            help = "The path to the hue input image") { trim() }.default("")

    val brightnessImageName by parser.storing(
            "-b", "--bri",
            help = "The path to the brightness input image") { trim() }.default("")

    val lsystem by parser.storing(
            "-s", "--lsystem",
            help = "What L system to use: " + getLSystemNames()) { trim() }.default("Hilbert")

    val lineWidth by parser.storing(
            "-w", "--lineWidth",
            help = "The width of the line") { trim().toDouble() }.default(1.0)

    private fun getLSystemNames(): String? {
        val lSystemInfo = Klaxon().parse<LSystemInfo>(File("src/main/resources/curves.json").readText())
        val systems = lSystemInfo?.systems
        val names = systems?.map { it.name }?.joinToString { it }
        return names
    }
}