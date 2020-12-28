import org.junit.Test
import se.kjellstrand.lsystem.LSystemGenerator
import se.kjellstrand.lsystem.LSystemRenderer
import se.kjellstrand.lsystem.model.LSystem
import se.kjellstrand.variablewidthline.LinePoint
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LSTest {

    @Test
    internal fun renderLSystem() {
        println("Init")
        val t0 = System.currentTimeMillis()

        var listOfSystemsToRender = //emptyList<String>()
            listOf("Hilbert")

        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = LSystem.SYSTEMS.map { it.name }
        }
        val imageNames = listOf("debug.jpg")

        for (imageName in imageNames) {
            val image = readImageFile("input/$imageName")
            for (systemName in listOfSystemsToRender) {
                getLSystemByName(systemName, LSystem.SYSTEMS)?.let { lSystem ->
                    var i = 1
                    var result = true
                    while (result) {
                        i++
                        println("----------- $imageName - $systemName - $i ----------- ")
                        result = renderLSystem(lSystem, i, imageName, image, 2000)
                    }
                    println("Break at i = $i")
                }
            }
        }

        val t1 = System.currentTimeMillis()
        println("Done after: " + (t1 - t0) + "ms\n")
    }

    fun renderLSystem(
        lSystem: LSystem,
        iteration: Int,
        brightnessImageName: String,
        brightnessImage: BufferedImage,
        outputImageSize: Int
    ): Boolean {
        val t0 = System.currentTimeMillis()

        val line = LSystemGenerator.generatePolygon(lSystem, iteration)
        val vwLine = line.map { linePoint -> LinePoint(linePoint.x, linePoint.y, 1.0) }

        val (minWidth, maxWidth) = LSystemRenderer.getRecommendedMinAndMaxWidth(outputImageSize, iteration, lSystem)

        val outputSideBuffer = outputImageSize / 50
        adjustToOutputRectangle(outputImageSize, outputSideBuffer, vwLine)

        if (minWidth < 0.5 || minWidth < outputImageSize / 5000) {
            return false
        }

        var bufferedImage = LSystemRenderer.renderLSystem(vwLine, brightnessImage, outputImageSize, minWidth, maxWidth)

        val fileName = getFirstPartOfImageName(brightnessImageName) +
                "_" + lSystem.name +
                "_iterations_" + iteration +
                "_size_" + outputImageSize.toInt() +
                "_lwe_" + lSystem.lineWidthExp

        val pngFileName = "output/$fileName.png"

        writeImageToPngFile(bufferedImage, pngFileName)
        val t1 = System.currentTimeMillis()
        println("Rendering and writing to file took: " + (t1 - t0) + "ms\n")
        return true
    }

    private fun adjustToOutputRectangle(
        outputImageSize: Int,
        outputSideBuffer: Int,
        vwLine: List<LinePoint>
    ) {
        val r = Rectangle(
            outputSideBuffer,
            outputSideBuffer,
            outputImageSize - outputSideBuffer * 2,
            outputImageSize - outputSideBuffer * 2
        )
        vwLine.forEach { point ->
            point.x = r.getX() + point.x.times(r.height)
            point.y = r.getY() + point.y.times(r.width)
        }
    }

    fun getLSystemByName(lSystemName: String, lSystems: List<LSystem>): LSystem? {
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
}