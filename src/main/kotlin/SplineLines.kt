import LSystem.PolyPoint
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.RenderingHints.*
import java.util.*


class SplineLines {

    // For 4 control points:
    //
    //P = (1−t)3P1 + 3(1−t)2tP2 +3(1−t)t2P3 + t3P4

    companion object {

        fun drawPolygonAsSplines(polygon: List<PolyPoint>,
                                 hueImage: BufferedImage?,
                                 brightnessImage: BufferedImage?,
                                 size: Double,
                                 lineWidth: Double,
                                 sidePadding: Double): BufferedImage {

            val t0 = System.currentTimeMillis()

            val (bufferedImage, g2) = setupGraphics(size, sidePadding)

            //  val polygonWithMidpoints = addMidPointsToPolygon(polygon)

            val polygonWithWidthAdjusted = adjustWidthAccordingToImage(polygon, brightnessImage)
            val polygonWithColor = setColorToPolygon(polygonWithWidthAdjusted, hueImage)
            //  var smoothWidthList = smoothOutWidthListForPolygon(rawWidthList)
            // var widthList = prepareWidthDataForDrawing(smoothWidthList)

            val t1 = System.currentTimeMillis()
            print("Prepare data for drawing: " + (t1 - t0) + "ms\n")

            // val polygonDoubleArrayList = preparePolygonDataForDrawing(polygonWithMidpoints)

            val t2 = System.currentTimeMillis()
            print("Generate midpoints: " + (t2 - t1) + "ms\n")

            val t3 = System.currentTimeMillis()
            print("Draw spline outlines: " + (t3 - t2) + "ms\n")

            drawThePolygon(g2, polygonWithColor, hueImage, size, lineWidth, sidePadding)

            // No drawing can be performed after this.
            g2.dispose()

            val t4 = System.currentTimeMillis()
            print("Draw splines: " + (t4 - t3) + "ms\n")

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

//        private fun smoothOutWidthListForPolygon(ppList: List<PolyPoint>): List<PolyPoint> {
//            return ppList.windowed(size = 5, step = 1, partialWindows = true) { window -> average(window) }.toMutableList()
//        }
//
//        private fun average(list: List<PolyPoint>): PolyPoint {
//            if (list.size == 1) {
//                return list[0]
//            }
//            var sum = 0.0
//            var fractionTotal = 0.0
//            for (i in 0 until list.size) {
//                val n = list.size.toDouble() - 1
//                val nn = (i / n) * 0.8 + 0.1
//                val fraction = Math.sin(nn * Math.PI)
//                fractionTotal += fraction
//                sum += list[i].w * fraction
//            }
//            return PolyPoint()sum / fractionTotal
//        }

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

                var pp1 = pp[Math.max(i - 1, 0)]
                var pp2 = PolyPoint(pp[i].x, pp[i].y, pp[i].w,
                        ColorUtils.getColorFromImage(pp[i].x, pp[i].y, hueImage))
                var pp3 = pp[Math.min(i + 1, pp.size - 1)]

                drawSpline(g2,
                        PolyPoint((pp1.x + pp2.x) / 2.0, (pp1.y + pp2.y) / 2.0, (pp1.w + pp2.w) / 2.0, ColorUtils.blend(pp1.c, pp2.c, 0.5)), // TODO Blend color as well, add p, p constuctor to handle averaging of points
                        pp2,
                        PolyPoint((pp2.x + pp3.x) / 2.0, (pp2.y + pp3.y) / 2.0, (pp2.w + pp3.w) / 2.0, ColorUtils.blend(pp2.c, pp3.c, 0.5)),
                        size, lineWidth, sidePadding)
            }
        }

        private fun adjustWidthAccordingToImage(polygon: List<PolyPoint>, image: BufferedImage?): List<PolyPoint> {
            val ppList = ArrayList<PolyPoint>()
            for (i in 0 until polygon.size) {
                var p = polygon[i]
                // Use the inverted brightness as width of the line we drawSpline.
                val c = (1 - ColorUtils.getBrightnessFromImage(p.x, p.y, image))
                println("c: " + c + " , pw " + p.w)
                ppList.add(PolyPoint(p.x, p.y, p.w * c))
            }
            return ppList
        }

        private fun drawSpline(g2: Graphics2D, pp1: PolyPoint, pp2: PolyPoint, pp3: PolyPoint,
                               size: Double, lineWidth: Double, sidePadding: Double) {

            println("p1: " + pp1 + "  p2: " + pp2 + "  p3: " + pp3)

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
    }
}