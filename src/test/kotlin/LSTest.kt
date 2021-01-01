import org.junit.Test
import se.kjellstrand.lsystem.LSystemGenerator
import se.kjellstrand.lsystem.LSystemRenderer
import se.kjellstrand.lsystem.model.LSystem
import se.kjellstrand.variablewidthline.LinePoint
import se.kjellstrand.variablewidthline.buildHullFromPolygon
import java.awt.*
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LSTest {

    @Test
    internal fun renderLSystems() {
        println("Init")
        val totalTime0 = System.currentTimeMillis()

        var listOfSystemsToRender = //emptyList<String>()
            listOf("Hilbert")

        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = LSystem.getSystems().map { it.name }
        }
        val imageNames = listOf("noice.jpg")

        for (imageName in imageNames) {
            val image = readImageFile("input/$imageName")
            for (systemName in listOfSystemsToRender) {
                LSystem.getByName(systemName)?.let { lSystem ->
                    var i = 1
                    var result = true
                    while (result) {
                        i++
                        print("Input image: $imageName, System: $systemName, Iteration: $i, ")

                        val processingTime0 = System.currentTimeMillis()
                        result = renderLSystem(lSystem, i, imageName, image, 2000)

                        val processingTime1 = System.currentTimeMillis()
                        println("processing time: " + (processingTime1 - processingTime0) + "ms\n")
                    }
                    println("Break at i = $i")
                }
            }
        }

        val totalTime1 = System.currentTimeMillis()
        println("Done after: " + (totalTime1 - totalTime0) + "ms\n")
    }

    private fun renderLSystem(
        lSystem: LSystem,
        iteration: Int,
        brightnessImageName: String,
        brightnessImage: BufferedImage,
        outputImageSize: Int
    ): Boolean {

        val line = LSystemGenerator.generatePolygon(lSystem, iteration)

        val vwLine = line.map { linePoint -> LinePoint(linePoint.x, linePoint.y, 1.0) }

        val (minWidth, maxWidth) = LSystemRenderer.getRecommendedMinAndMaxWidth(outputImageSize, iteration, lSystem)

        if (minWidth < 0.5 || minWidth < outputImageSize / 5000) {
            return false
        }

        val outputSideBuffer = outputImageSize / 50
        adjustToOutputRectangle(outputImageSize, outputSideBuffer, vwLine)


        val ly = Array(brightnessImage.height) { ByteArray(brightnessImage.width) }
        for (y in 0 until brightnessImage.height) {
            val lx = ByteArray(brightnessImage.width)
            ly[y] = lx
            for (x in 0 until brightnessImage.width) {
                lx[x] = getBrightnessFromImage(y, x, brightnessImage)
            }
        }


        val bufferedImage = generateBitmapFromLSystem(vwLine, ly, outputImageSize, minWidth, maxWidth)

        val fileName = getFirstPartOfImageName(brightnessImageName) +
                "_" + lSystem.name +
                "_iterations_" + iteration +
                "_size_" + outputImageSize +
                "_lwe_" + lSystem.lineWidthExp

        val pngFileName = "output/$fileName.png"

        writeImageToPngFile(bufferedImage, pngFileName)
        return true
    }

    private fun getBrightnessFromImage(x: Int, y: Int, image: BufferedImage): Byte {
        var color = 0x777777
        try {
            color = image.getRGB(x, y)
        } catch (e: Exception) {
        }
        var c = FloatArray(3)
        Color.RGBtoHSB(
            color shr 16 and 255,
            color shr 8 and 255,
            color and 255,
            c
        )
        return (c[2] * 256).toInt().toByte()
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

    private fun generateBitmapFromLSystem(
        line: List<LinePoint>,
        luminanceData: Array<ByteArray>,
        outputImageSize: Int,
        minWidth: Double,
        maxWidth: Double
    ): BufferedImage {

        val (bufferedImage, g2) = setupGraphics(outputImageSize)

        LSystemRenderer.adjustLineWidthAccordingToImage(line, luminanceData, outputImageSize, minWidth, maxWidth)

        val vwLine = buildHullFromPolygon(line)

        drawPolygon(vwLine, g2)

        tearDownGraphics(g2)

        return bufferedImage
    }

    private fun drawPolygon(hull: MutableList<LinePoint>, g2: Graphics2D) {
        val path = GeneralPath()
        val polygonInitialPP = LinePoint.getMidPoint(hull[hull.size - 1], hull[hull.size - 2])
        path.moveTo(polygonInitialPP.x, polygonInitialPP.y)

        for (i in 0 until hull.size) {
            val quadStartPP = hull[(if (i == 0) hull.size else i) - 1]
            val nextQuadStartPP = hull[i]
            val quadEndPP = LinePoint.getMidPoint(quadStartPP, nextQuadStartPP)
            path.quadTo(quadStartPP.x, quadStartPP.y, quadEndPP.x, quadEndPP.y)
        }
        path.closePath()

        g2.paint = Color.BLACK

        g2.fill(path)
    }

    private fun tearDownGraphics(g2: Graphics2D) {
        g2.dispose()
    }

    private fun setupGraphics(size: Int): Pair<BufferedImage, Graphics2D> {
        val bufferedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

        val g2 = bufferedImage.createGraphics()
        val rh = mutableMapOf<RenderingHints.Key, Any>()
        rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
        rh[RenderingHints.KEY_ALPHA_INTERPOLATION] = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        rh[RenderingHints.KEY_COLOR_RENDERING] = RenderingHints.VALUE_COLOR_RENDER_QUALITY
        rh[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
        rh[RenderingHints.KEY_STROKE_CONTROL] = RenderingHints.VALUE_STROKE_PURE
        g2.setRenderingHints(rh)

        g2.stroke = BasicStroke(2f)
        g2.color = Color.WHITE
        g2.fill(Rectangle(0, 0, bufferedImage.width, bufferedImage.height))
        return Pair(bufferedImage, g2)
    }
}