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

        fun drawPolygonAsSplines(polygon: List<Pair<Double, Double>>,
                                 hueImage: BufferedImage?,
                                 lightnessImage: BufferedImage?,
                                 size: Double,
                                 sidePadding: Double,
                                 lineWidth: Double,
                                 outlineWidth: Double,
                                 debug: Boolean): java.awt.image.BufferedImage {

            val t0 = System.currentTimeMillis()

            val (bufferedImage, g2) = setupGraphics(size, sidePadding)

            val polygonWithMidpoints = addMidPointsToPolygon(polygon)

            var rawWidthList = getWidthListForPolygon(polygonWithMidpoints, lightnessImage)
            var smoothWidthList = smoothOutWidthListForPolygon(rawWidthList)
            var widthList = prepareWidthDataForDrawing(smoothWidthList)

            val t1 = System.currentTimeMillis()
            print("Prepare data for drawing: " + (t1 - t0) + "ms\n")

            val polygonDoubleArrayList = preparePolygonDataForDrawing(polygonWithMidpoints)

            val t2 = System.currentTimeMillis()
            print("Generate midpoints: " + (t2 - t1) + "ms\n")

            if (outlineWidth > 0) {
                drawThePolygonOutline(g2, polygonDoubleArrayList, size, widthList, sidePadding, lineWidth, outlineWidth)
            }

            val t3 = System.currentTimeMillis()
            print("Draw spline outlines: " + (t3 - t2) + "ms\n")

            drawThePolygon(g2, polygonDoubleArrayList, hueImage, size, widthList, sidePadding, lineWidth)

            if (debug) {
                drawDebugPoints(g2, polygonDoubleArrayList, size, sidePadding)
            }

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

        private fun smoothOutWidthListForPolygon(rawWidthListForPolygon: MutableList<Double>): MutableList<Double> {
            return rawWidthListForPolygon.windowed(size = 5, step = 1, partialWindows = true) { window -> average(window) }.toMutableList()
        }

        private fun average(list: List<Double>): Double {
            if (list.size == 1) {
                return list[0]
            }
            var sum = 0.0
            var fractionTotal = 0.0
            for (i in 0 until list.size) {
                val n = list.size.toDouble() - 1
                val nn = (i / n) * 0.8 + 0.1
                val fraction = Math.sin(nn * Math.PI)
                fractionTotal += fraction
                sum += list[i] * fraction
            }
            return sum / fractionTotal
        }

        private fun drawThePolygonOutline(g2: Graphics2D, polygonPoints: MutableList<DoubleArray>, size: Double,
                                          widthList: MutableList<DoubleArray>, sidePadding: Double, lineWidth: Double, outlineWidth: Double) {
            var colors = listOf(Color.BLACK, Color.BLACK, Color.BLACK)
            for (i in 0 until polygonPoints.size) {
                drawSpline(g2, polygonPoints[i], widthList[i], colors, sidePadding, size, lineWidth, outlineWidth)
            }
        }

        private fun drawThePolygon(g2: Graphics2D, polygonPoints: MutableList<DoubleArray>, hueImage: BufferedImage?, size: Double,
                                   widthList: MutableList<DoubleArray>, sidePadding: Double, lineWidth: Double) {
            for (i in 0 until polygonPoints.size) {
                var colors = listOf(
                        ColorUtils.getColorFromImage(polygonPoints[i][0], polygonPoints[i][1], hueImage),
                        ColorUtils.getColorFromImage(polygonPoints[i][2], polygonPoints[i][3], hueImage),
                        ColorUtils.getColorFromImage(polygonPoints[i][4], polygonPoints[i][5], hueImage))

                drawSpline(g2, polygonPoints[i], widthList[i], colors, sidePadding, size, lineWidth, 0.0)
            }
        }

        private fun drawDebugPoints(g2: Graphics2D, polygonPoints: MutableList<DoubleArray>, size: Double, sidePadding: Double) {
            g2.color = Color.CYAN
            for (i in 0 until polygonPoints.size) {
                drawDebugPoint(g2, polygonPoints[i], size, sidePadding)
                g2.color = Color.BLACK
            }
        }

        private fun preparePolygonDataForDrawing(polygonWithMidpoints: List<Pair<Double, Double>>): MutableList<DoubleArray> {
            var listOfDoubleArrays = mutableListOf<DoubleArray>()
            for (i in 0 until polygonWithMidpoints.size step 2) {
                val p0 = polygonWithMidpoints[Math.max(i - 1, 0)]
                val p1 = polygonWithMidpoints[i]
                val p2 = polygonWithMidpoints[Math.min(i + 1, polygonWithMidpoints.size - 1)]

                val polygonPoints = doubleArrayOf(
                        p0.first, p0.second,
                        p1.first, p1.second,
                        p2.first, p2.second
                )
                listOfDoubleArrays = (listOfDoubleArrays + polygonPoints).toMutableList()

            }
            return listOfDoubleArrays
        }

        private fun prepareWidthDataForDrawing(widthPoints: List<Double>): MutableList<DoubleArray> {
            var listOfDoubleArrays = mutableListOf<DoubleArray>()
            for (i in 0 until widthPoints.size step 2) {
                val d0 = widthPoints[Math.max(i - 1, 0)]
                val d1 = widthPoints[i]
                val d2 = widthPoints[Math.min(i + 1, widthPoints.size - 1)]

                val polygonPoints = doubleArrayOf(
                        d0, d1, d2
                )
                listOfDoubleArrays = (listOfDoubleArrays + polygonPoints).toMutableList()

            }
            return listOfDoubleArrays
        }

        private fun getWidthListForPolygon(polygon: List<Pair<Double, Double>>, lightnessImage: BufferedImage?): MutableList<Double> {
            var widthList = mutableListOf<Double>()
            for (i in 0 until polygon.size) {
                val p = polygon[i]
                // Use the inverted brightness as width of the line we drawSpline.
                widthList.add(1 - ColorUtils.getLightnessFromImage(p.first, p.second, lightnessImage))
            }
            return widthList
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

        private fun drawSpline(g2: Graphics2D, polygonPoints: DoubleArray, widthList: DoubleArray, colors: List<Color>,
                               sidePadding: Double, size: Double, lineWidth: Double, outlineWidth: Double) {

            if (lineWidth == 0.0) {
                return
            }

            val euclideanDistance = Math.sqrt(
                    Math.abs(Math.pow(polygonPoints[0] - polygonPoints[2], 2.0) +
                            Math.pow(polygonPoints[1] - polygonPoints[3], 2.0)))

            var t = 0.0
            while (t <= 1.0) {
                // The bezier square spline is calculated using this formula from wikipedia.
                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

                // Calculate the Bezier (x, y) coordinate for this step.
                val x = (Math.pow((1 - t), 2.0) * polygonPoints[0]) +
                        (2 * (1 - t) * t * polygonPoints[2]) +
                        (Math.pow(t, 2.0) * polygonPoints[4])
                val y = (Math.pow((1 - t), 2.0) * polygonPoints[1]) +
                        (2 * (1 - t) * t * polygonPoints[3]) +
                        (Math.pow(t, 2.0) * polygonPoints[5])

                // Calculate the Bezier width for this step.
                val width = Math.max(1.0,
                        ((Math.pow((1 - t), 2.0) * widthList[0]) +
                                (2 * (1 - t) * t * widthList[1]) +
                                (Math.pow(t, 2.0) * widthList[2])) * lineWidth) + outlineWidth

                // Crate a circle at the right spot and size
                val circle = Ellipse2D.Double(
                        ((x * size) - width / 2) + sidePadding,
                        ((y * size) - width / 2) + sidePadding,
                        width, width)

                // Calculate the color of the circle for this step.
                var color: Color = when {
                    t < 0.5 -> ColorUtils.blend(colors[0], colors[1], 1.0 - t * 2)
                    else -> ColorUtils.blend(colors[1], colors[2], 1.0 - (t - 0.5) * 2)
                }

                // Set the color of the circle.
                g2.color = color

                // Draw the circle.
                g2.fill(circle)

                // Calculate the t value used in the Bezier calculations above.
                t += (width / 8.0) / (Math.max(1.0, euclideanDistance) * size)
            }
        }

        private fun drawDebugPoint(g2: Graphics2D, polygonPoints: DoubleArray, size: Double, sidePadding: Double) {
            g2.fill(Ellipse2D.Double(
                    ((polygonPoints[0] * size) - 2.5) + sidePadding,
                    ((polygonPoints[1] * size) - 2.5) + sidePadding,
                    5.0, 5.0))

            g2.fill(Ellipse2D.Double(
                    ((polygonPoints[2] * size) - 2.5) + sidePadding,
                    ((polygonPoints[3] * size) - 2.5) + sidePadding,
                    5.0, 5.0))
        }
    }
}