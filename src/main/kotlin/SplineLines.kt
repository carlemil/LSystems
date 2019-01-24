import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage


class SplineLines {

    val circle = Ellipse2D.Double()

    // For 4 control points:
    //
    //P = (1−t)3P1 + 3(1−t)2tP2 +3(1−t)t2P3 + t3P4

    companion object {

        fun drawPolygonAsSplines(polygon: List<Pair<Double, Double>>,
                                 inputImage: BufferedImage?,
                                 size: Double,
                                 sidePadding: Double,
                                 palette: IntArray): java.awt.image.BufferedImage {

            val bufferedImage = BufferedImage((size + sidePadding * 2).toInt(), (size + sidePadding * 2).toInt(),
                    BufferedImage.TYPE_INT_RGB)

            val g2 = bufferedImage.createGraphics()
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.stroke = BasicStroke(2f)
            g2.color = Color.WHITE
            g2.fill(Rectangle(0, 0, bufferedImage.width, bufferedImage.height))
            g2.color = Color.BLACK

            val polygonWithMidpoints = addMidPointsToPolygon(polygon)

            for (i in 1 until polygonWithMidpoints.size - 2 step 2) {
                val p0 = polygonWithMidpoints[i]
                val p1 = polygonWithMidpoints[i + 1]
                val p2 = polygonWithMidpoints[i + 2]

                drawSpline(g2, 40,
                        doubleArrayOf(
                                p0.first, p0.second,
                                p1.first, p1.second,
                                p2.first, p2.second),
                        doubleArrayOf(
                                // Use the inverted brightness as width of the line we drawSpline.
                                (1 - getBrightnessFromImage(p1.first, p1.second, inputImage)) * 11 + 1
                        ),
                        size)
            }
            g2.dispose()
            return bufferedImage
        }

        private fun addMidPointsToPolygon(coordList: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
            val resultingCoordList = ArrayList<Pair<Double, Double>>()

            for (i in 0 until coordList.size - 1) {
                val c0 = coordList[i]
                val c1 = coordList[i + 1]
                val cMid = Pair((c0.first + c1.first) / 2.0, (c0.second + c1.second) / 2.0)
                resultingCoordList.add(c0)
                resultingCoordList.add(cMid)
            }
            resultingCoordList.add(coordList.last())

            return resultingCoordList
        }

        private fun drawSpline(g2: Graphics2D,
                               drawSteps: Int,
                               polygonPoints: DoubleArray,
                               widthForPoints: DoubleArray,
                               size: Double) {
            for (i in 0 until drawSteps) {
                // Calculate the Bezier (x, y) coordinate for this step.
                val t = (i.toFloat() / drawSteps).toDouble()

                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3
                val x = (Math.pow((1 - t), 2.0) * polygonPoints[0]) +
                        (2 * (1 - t) * t * polygonPoints[2]) +
                        (Math.pow(t, 2.0) * polygonPoints[4])
                val y = (Math.pow((1 - t), 2.0) * polygonPoints[1]) +
                        (2 * (1 - t) * t * polygonPoints[3]) +
                        (Math.pow(t, 2.0) * polygonPoints[5])

                var radius = widthForPoints[0]

                val circle = Ellipse2D.Double(((x * size) - radius / 2), ((y * size) - radius / 2),
                        radius, radius)

                g2.fill(circle)
            }
        }

        private fun getBrightnessFromImage(inX: Double, inY: Double, image: BufferedImage?): Double {
            var color = 0xffffff
            if (image != null) {
                val x = (inX * (image.width - 1.0)).toInt()
                val y = (inY * (image.height - 1.0)).toInt()
                color = image.getRGB(x, y)
            }
            var c = FloatArray(3)
            Color.RGBtoHSB(
                    color shr 16 and 255,
                    color shr 8 and 255,
                    color and 255,
                    c)
            return c[2].toDouble()
        }
    }
}