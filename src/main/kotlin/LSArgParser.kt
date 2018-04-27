import LSystem.*
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class LSArgParser(parser: ArgParser) {

    val iterations by parser.storing(
            "-i", "--iterations",
            help = "Number of iterations of the L system") { toInt() }.default(4)

    val outputImageSize by parser.storing(
            "-S", "--outputImageSize",
            help = "Size of the output svg image (in pixels)") { toDouble() }.default(800.0)

    val useBezierCurves by parser.storing(
            "-b", "--useBezierCurves",
            help = "Draw the L system using bezier curves, not straight lines") { toBoolean() }.default(false)

    val themeName by parser.storing(
            "-t", "--themeName",
            help = "The name of the theme, default \"hsv_gradient_checkered\", look in Themes.kt for more themes") { toString() }.default("hsv_gradient_checkered")

    val imageName by parser.storing(
            "-p", "--imageName",
            help = "The path to the input image") { toString() }.default("")

    val lsystem by parser.storing(
            "-s", "--system",
            help = "What L system to use, default: \"" + dragonLSystem().getName() + "\"" +
                    " other curves: " + getLSystemNames()) { toString() }.default(dragonLSystem().getName())

    val paletteRepeat by parser.storing(
            "-r", "--paletteRepeat",
            help = "The repeat frequency of the palette (1)") { toDouble() }.default(1.0)

    val lineWidth by parser.storing(
            "-w", "--lineWidth",
            help = "The width of the line") { toDouble() }.default(1.0)


    private fun getLSystemNames(): String {
        return kochSnowFlakeLSystem().getName() + ", " + hilbertLSystem().getName() + ", " +
                lineLSystem().getName() + ", " + sierpinskiLSystem().getName() + ", " +
                snowFlake1LSystem().getName()
    }

}