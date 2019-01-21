import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage


class DrawLine {

    val circle = Ellipse2D.Double()

    companion object {
        fun paint(coordList: List<Pair<Double, Double>>, size: Double, sidePadding: Double, palette: IntArray) {
            val bufferedImage = BufferedImage((size + sidePadding * 2).toInt(), (size + sidePadding * 2).toInt(),
                    BufferedImage.TYPE_INT_RGB)
            val g2 = bufferedImage.createGraphics()
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2!!.stroke = BasicStroke(2f)
            g2.color = Color.WHITE

            for (i in 1..coordList.size - 2 step 2) {
                val p0 = coordList.get(i - 1)
                val p1 = coordList.get(i)
                val p2 = coordList.get(i + 1)

                //g2.draw(Line2D.Double(x0, y0, x1, y1))
                draw(g2, 1.0, 16.0, 40,
                        p0, p1, p2, size)
            }

            g2.dispose()

            val file = File("newimage.png")
            ImageIO.write(bufferedImage, "png", file)

        }

        fun draw(g2: Graphics2D, startWidth: Double, endWidth: Double, drawSteps: Int,
                 p0: Pair<Double, Double>, p1: Pair<Double, Double>, p2: Pair<Double, Double>, size: Double) {
//        val originalWidth = paint.getStrokeWidth()
            val widthDelta = endWidth - startWidth
            for (i in 0 until drawSteps) {
                // Calculate the Bezier (x, y) coordinate for this step.
                val t = (i.toFloat() / drawSteps).toDouble()

                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3
                val x = (Math.pow((1 - t), 2.0) * p0.first) +
                        (2 * (1 - t) * t * p1.first) +
                        (Math.pow(t, 2.0) * p2.first)
                val y = (Math.pow((1 - t), 2.0) * p0.second) +
                        (2 * (1 - t) * t * p1.second) +
                        (Math.pow(t, 2.0) * p2.second)

                val radius = startWidth * t + endWidth * (1 - t)
                val circle = Ellipse2D.Double(((x * size) - radius / 2), ((y * size) - radius / 2),
                        radius, radius)


                g2.fill(circle)
            }
//        paint.setStrokeWidth(originalWidth)
        }
    }
}