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
            g2.color = Color.RED

            for (i in 1..coordList.size - 3 step 3) {
                val p0 = coordList.get(i - 1)
                val p1 = coordList.get(i)
                val p2 = coordList.get(i + 1)
                val p3 = coordList.get(i + 2)
                val x0 = p0.first * size + sidePadding
                val y0 = p0.second * size + sidePadding
                val x1 = p1.first * size + sidePadding
                val y1 = p1.second * size + sidePadding
                val x2 = p2.first * size + sidePadding
                val y2 = p2.second * size + sidePadding
                val x3 = p3.first * size + sidePadding
                val y3 = p3.second * size + sidePadding
                //g2.draw(Line2D.Double(x0, y0, x1, y1))
                draw(g2, 1.0, 4.0, 20,
                        Point2D.Double(x0, y0), Point2D.Double(x1, y1), Point2D.Double(x2, y2), Point2D.Double(x3, y3))
            }

            g2.dispose()

            val file = File("newimage.png")
            ImageIO.write(bufferedImage, "png", file)

        }

        fun draw(g2: Graphics2D, startWidth: Double, endWidth: Double, drawSteps: Int,
                 startPoint: Point2D, control1: Point2D, control2: Point2D, endPoint: Point2D) {
//        val originalWidth = paint.getStrokeWidth()
            val widthDelta = endWidth - startWidth
            for (i in 0 until drawSteps) {
                // Calculate the Bezier (x, y) coordinate for this step.
                val t = i.toFloat() / drawSteps
                val tt = t * t
                val ttt = tt * t
                val u = 1 - t
                val uu = u * u
                val uuu = uu * u
                var x = uuu * startPoint.x
                x += 3 * uu * t * control1.x
                x += 3 * u * tt * control2.x
                x += ttt * endPoint.x
                var y = uuu * startPoint.y
                y += 3 * uu * t * control1.y
                y += 3 * u * tt * control2.y
                y += ttt * endPoint.y
                // Set the incremental stroke width and draw.
//            g2.setStrokeWidth(startWidth + ttt * widthDelta)
                val radius = 2
                val circle = Ellipse2D.Double((x - radius).toDouble(), (y - radius).toDouble(),
                        2.0 * radius, 2.0 * radius)

                g2.draw(circle)
            }
//        paint.setStrokeWidth(originalWidth)
        }
    }
}