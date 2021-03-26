import org.junit.Test
import se.kjellstrand.lsystem.LSystemGenerator
import se.kjellstrand.lsystem.LSystemGenerator.getRecommendedMinAndMaxWidth
import se.kjellstrand.lsystem.LSystemGenerator.setLineWidthAccordingToImage
import se.kjellstrand.lsystem.buildHullFromPolygon
import se.kjellstrand.lsystem.getMidPoint
import se.kjellstrand.lsystem.model.LSTriple
import se.kjellstrand.lsystem.model.LSystem
import java.awt.*
import java.awt.geom.AffineTransform
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
            listOf("Cross")

        if (listOfSystemsToRender.isEmpty()) {
            listOfSystemsToRender = LSystem.systems.map { it.name }
        }
        val imageNames = listOf("str.jpg")

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

        var line = LSystemGenerator.generatePolygon(lSystem, iteration)

        val (minWidth, maxWidth) = getRecommendedMinAndMaxWidth(iteration, lSystem)

        if (minWidth < 0.001 || minWidth < outputImageSize / 5000) {
            return false
        }

        val luminance = Array(brightnessImage.height) { DoubleArray(brightnessImage.width) }
        for (y in 0 until brightnessImage.height) {
            val lx = DoubleArray(brightnessImage.width)
            luminance[y] = lx
            for (x in 0 until brightnessImage.width) {
                lx[x] = getBrightnessFromImage(y, x, brightnessImage)
            }
        }

        val bufferedImage = generateBitmapFromLSystem(line, luminance, outputImageSize, minWidth, maxWidth)

        val fileName = getFirstPartOfImageName(brightnessImageName) +
                "_" + lSystem.name +
                "_iterations_" + iteration +
                "_size_" + outputImageSize +
                "_lwe_" + lSystem.lineWidthExp

        val pngFileName = "output/$fileName.png"

        writeImageToPngFile(bufferedImage, pngFileName)
        return true
    }

    private fun getBrightnessFromImage(x: Int, y: Int, image: BufferedImage): Double {
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
        return 1.0 - c[2]
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
        line: MutableList<LSTriple>,
        luminanceData: Array<DoubleArray>,
        outputImageSize: Int,
        minWidth: Double,
        maxWidth: Double
    ): BufferedImage {

        val (bufferedImage, g2) = setupGraphics(outputImageSize)

        setLineWidthAccordingToImage(line, luminanceData, minWidth, maxWidth)

        LSystemGenerator.addSideBuffer(maxWidth, line)

        LSystemGenerator.smoothenWidthOfLine(line)

        val hull = buildHullFromPolygon(line)

        drawPolygon(hull, g2, outputImageSize.toDouble())

        tearDownGraphics(g2)

        return bufferedImage
    }

    private fun drawPolygon(hull: MutableList<LSTriple>, g2: Graphics2D, outputImageSize: Double) {
        val path = GeneralPath()
        val polygonInitialPP = getMidPoint(hull[hull.size - 1], hull[hull.size - 2])
        path.moveTo(polygonInitialPP.x, polygonInitialPP.y)

        for (i in 0 until hull.size) {
            val quadStartPP = hull[(if (i == 0) hull.size else i) - 1]
            val nextQuadStartPP = hull[i]
            val quadEndPP = getMidPoint(quadStartPP, nextQuadStartPP)
            path.quadTo(quadStartPP.x, quadStartPP.y, quadEndPP.x, quadEndPP.y)
        }
        path.closePath()

        g2.paint = Color.BLACK

        val af = AffineTransform()
        af.scale(outputImageSize, outputImageSize)
        path.transform(af)
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