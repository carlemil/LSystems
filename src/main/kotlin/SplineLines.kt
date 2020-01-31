import LSystem.PolyPoint
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.RenderingHints.*
import java.util.*


class SplineLines {

    companion object {

        fun drawPolygonAsSplines(polyPointList: List<PolyPoint>,
                                 hueImage: BufferedImage?,
                                 brightnessImage: BufferedImage?,
                                 size: Double,
                                 lineWidth: Double,
                                 sidePadding: Double): BufferedImage {

            val t0 = System.currentTimeMillis()
            val (bufferedImage, g2) = setupGraphics(size, sidePadding)

            val polygon = Polygon()
            polyPointList.forEach { p ->
                val width = (p.w + p.w) / 2.0
                polygon.addPoint(
                        (((p.x * size) - width / 2.0) + sidePadding).toInt(),
                        (((p.y * size) - width / 2.0) + sidePadding).toInt()
                )
            }


            g2.drawPolygon(polygon)

            //val polygonWithWidthAdjusted = adjustWidthAccordingToImage(polygon, brightnessImage)
            //val polygonWithColor = setColorToPolygon(polygonWithWidthAdjusted, hueImage)

            val t1 = System.currentTimeMillis()
            print("Prepare data for drawing: " + (t1 - t0) + "ms\n")

            //drawThePolygon(g2, polygonWithColor, hueImage, size, lineWidth, sidePadding)

            // No drawing can be performed after this.
            g2.dispose()

            val t2 = System.currentTimeMillis()
            print("Draw splines: " + (t2 - t1) + "ms\n")

            return bufferedImage
        }

        private fun setupGraphics(size: Double, sidePadding: Double): Pair<BufferedImage, Graphics2D> {
            val bufferedImage = BufferedImage((size + sidePadding * 2).toInt(), (size + sidePadding * 2).toInt(),
                    BufferedImage.TYPE_INT_RGB)

            val g2 = bufferedImage.createGraphics()
            val rh = mutableMapOf<Key, Any>()
            rh[KEY_ANTIALIASING] = VALUE_ANTIALIAS_ON
            rh[KEY_ALPHA_INTERPOLATION] = VALUE_ALPHA_INTERPOLATION_QUALITY
            rh[KEY_COLOR_RENDERING] = VALUE_COLOR_RENDER_QUALITY
            rh[KEY_RENDERING] = VALUE_RENDER_QUALITY
            rh[KEY_STROKE_CONTROL] = VALUE_STROKE_PURE
            g2.setRenderingHints(rh)

            g2.stroke = BasicStroke(2f)
            g2.color = Color.WHITE
            g2.fill(Rectangle(0, 0, bufferedImage.width, bufferedImage.height))
            g2.color = Color.BLACK
            return Pair(bufferedImage, g2)
        }

        private fun setColorToPolygon(pp: List<PolyPoint>, hueImage: BufferedImage?): List<PolyPoint> {
            val ppOut = mutableListOf<PolyPoint>()
            for (i in 0 until pp.size) {
                val p = pp[i]
                ppOut.add(PolyPoint(p.x, p.y, p.w, ColorUtils.getColorFromImage(p.x, p.y, hueImage)))
            }
            return ppOut
        }

        private fun drawThePolygon(g2: Graphics2D, pp: List<PolyPoint>, hueImage: BufferedImage?,
                                   size: Double, lineWidth: Double, sidePadding: Double) {

            for (i in 0 until pp.size) {
                // Get 3 points and set color to them
                var tmp0 = pp[Math.max(i - 1, 0)]
                val p0 = PolyPoint(tmp0.x, tmp0.y, tmp0.w, ColorUtils.getColorFromImage(tmp0.x, tmp0.y, hueImage))

                var tmp1 = pp[i]
                val p1 = PolyPoint(tmp1.x, tmp1.y, tmp1.w, ColorUtils.getColorFromImage(tmp1.x, tmp1.y, hueImage))

                val tmp2 = pp[Math.min(i + 1, pp.size - 1)]
                val p2 = PolyPoint(tmp2.x, tmp2.y, tmp2.w, ColorUtils.getColorFromImage(tmp2.x, tmp2.y, hueImage))

                // Calculate intermediate draw points
                val dp0 = PolyPoint.average(p0, p1)
                val dp1 = p1
                val dp2 = PolyPoint.average(p1, p2)

                drawLine(g2, p0, p1, size, lineWidth, sidePadding)

                // Draw spline segment
                //drawSpline(g2, dp0, dp1, dp2, size, lineWidth, sidePadding)
            }
        }

        private fun drawLine(g2: Graphics2D, pp1: PolyPoint, pp2: PolyPoint,
                             size: Double, lineWidth: Double, sidePadding: Double) {
            g2.color = ColorUtils.blend(pp1.c, pp2.c, 0.5)
            val width = (pp1.w + pp2.w) / 2.0
            g2.stroke = BasicStroke((width * lineWidth).toFloat())

            g2.drawLine(
                    (((pp1.x * size) - width / 2.0) + sidePadding).toInt(),
                    (((pp1.y * size) - width / 2.0) + sidePadding).toInt(),
                    (((pp2.x * size) - width / 2.0) + sidePadding).toInt(),
                    (((pp2.y * size) - width / 2.0) + sidePadding).toInt())

        }

        private fun adjustWidthAccordingToImage(polygon: List<PolyPoint>, image: BufferedImage?): List<PolyPoint> {
            val ppList = ArrayList<PolyPoint>()
            for (i in 0 until polygon.size) {
                var p = polygon[i]
                // Use the inverted brightness as width of the line we drawSpline.
                val c = (1 - ColorUtils.getBrightnessFromImage(p.x, p.y, image))
                ppList.add(PolyPoint(p.x, p.y, p.w * c))
            }
            return ppList
        }

        private fun drawSpline(g2: Graphics2D, pp1: PolyPoint, pp2: PolyPoint, pp3: PolyPoint,
                               size: Double, lineWidth: Double, sidePadding: Double) {

            val euclideanDistance = Math.sqrt(
                    Math.abs(Math.pow(pp1.x - pp2.x, 2.0) +
                            Math.pow(pp1.y - pp2.y, 2.0)))

            var t = 0.0
            while (t <= 1.0) {
                // The bezier square spline is calculated using this formula from wikipedia.
                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

                // Calculate the Bezier (x, y) coordinate for this step.
                val x = (Math.pow((1 - t), 2.0) * pp1.x) +
                        (2 * (1 - t) * t * pp2.x) +
                        (Math.pow(t, 2.0) * pp3.x)
                val y = (Math.pow((1 - t), 2.0) * pp1.y) +
                        (2 * (1 - t) * t * pp2.y) +
                        (Math.pow(t, 2.0) * pp3.y)

                // Calculate the Bezier width for this step.
                val width = Math.max(1.0,
                        ((Math.pow((1 - t), 2.0) * pp1.w) +
                                (2 * (1 - t) * t * pp2.w) +
                                (Math.pow(t, 2.0) * pp3.w)) * lineWidth)

                // Crate a circle at the right spot and size
                val circle = Ellipse2D.Double(
                        ((x * size) - width / 2) + sidePadding,
                        ((y * size) - width / 2) + sidePadding,
                        width, width)

                // Calculate the color of the circle for this step.
                var color: Color = when {
                    t < 0.5 -> ColorUtils.blend(pp1.c, pp2.c, 1.0 - t * 2)
                    else -> ColorUtils.blend(pp2.c, pp3.c, 1.0 - (t - 0.5) * 2)
                }

                // Set the color of the circle.
                g2.color = color

                // Draw the circle.
                g2.fill(circle)

                // Calculate the t value used in the Bezier calculations above.
                t += (width / 2.0) / (Math.max(1.0, euclideanDistance) * size)
            }
        }

        // For 4 control points: P = (1−t)3P1 + 3(1−t)2tP2 +3(1−t)t2P3 + t3P4

    }
}