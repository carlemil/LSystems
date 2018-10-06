import javax.swing.*
import java.awt.*
import java.awt.geom.Line2D
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage


class DrawLine {

    companion object {
        fun paint(coordList: List<Pair<Double, Double>>, size: Double) {
            val bufferedImage = BufferedImage(size.toInt(), size.toInt(), BufferedImage.TYPE_INT_RGB)
            val g2 = bufferedImage.createGraphics()

            g2!!.stroke = BasicStroke(2f)
            g2.color = Color.RED

            val size = 200

            for (i in 1..coordList.size - 1) {
                val p0 = coordList.get(i - 1)
                val p1 = coordList.get(i)
                val x0 = p0.first * size
                val y0 = p0.second * size
                val x1 = p1.first * size
                val y1 = p1.second * size
                g2.draw(Line2D.Double(x0, y0, x1, y1))
            }

            g2.dispose()

            val file = File("newimage.png")
            ImageIO.write(bufferedImage, "png", file)

        }
    }
}