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
                                 hueImage: BufferedImage?,
                                 lightnessImage: BufferedImage?,
                                 size: Double,
                                 sidePadding: Double,
                                 lineWidth: Double,
                                 outlineWidth: Double): java.awt.image.BufferedImage {

            val t0 = System.currentTimeMillis()

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

            var widthListForPolygon = getWidthListForPolygon(polygonWithMidpoints, lightnessImage)

            val t1 = System.currentTimeMillis()

            val polygonDoubleArrayList = preparePolygonDataForDrawing(polygonWithMidpoints)

            val t2 = System.currentTimeMillis()
            print("Generate midpoints: " + (t2 - t1) + "ms\n")

            drawThePolygonOutline(g2, polygonDoubleArrayList, size, widthListForPolygon, sidePadding, lineWidth)

            val t3 = System.currentTimeMillis()
            print("Draw spline outlines: " + (t3 - t2) + "ms\n")

            drawThePolygon(lineWidth, outlineWidth, polygonDoubleArrayList, hueImage, g2, size, widthListForPolygon, sidePadding)
            g2.dispose()

            val t4 = System.currentTimeMillis()
            print("Draw splines: " + (t4 - t3) + "ms\n")
            print("Draw total: " + (t4 - t0) + "ms\n")

            return bufferedImage
        }

        private fun drawThePolygonOutline(g2: Graphics2D, allPolygonPoints: MutableList<DoubleArray>, size: Double,
                                          allWidthForPoints: MutableList<DoubleArray>, sidePadding: Double, lineWidth: Double) {
            var colors = listOf(Color.BLACK, Color.BLACK, Color.BLACK)
            for (i in 0 until allPolygonPoints.size) {
                drawSpline(g2, allPolygonPoints[i], allWidthForPoints[i], colors, sidePadding, size, lineWidth)
            }
        }

        private fun drawThePolygon(lineWidth: Double, outlineWidth: Double, polygonPoints: MutableList<DoubleArray>,
                                   hueImage: BufferedImage?, g2: Graphics2D, size: Double,
                                   allWidthForPoints: MutableList<DoubleArray>, sidePadding: Double) {
            val width = lineWidth - outlineWidth
            if (width > 0) {
                listOf<Color>()
                for (i in 0 until polygonPoints.size) {
                    var colors = listOf(
                            ColorUtils.getColorFromImage(polygonPoints[i][0], polygonPoints[i][1], hueImage),
                            ColorUtils.getColorFromImage(polygonPoints[i][2], polygonPoints[i][3], hueImage),
                            ColorUtils.getColorFromImage(polygonPoints[i][4], polygonPoints[i][5], hueImage))

                    drawSpline(g2, polygonPoints[i], allWidthForPoints[i], colors, sidePadding, size, width)
                }
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

        private fun getWidthListForPolygon(polygon: List<Pair<Double, Double>>, lightnessImage: BufferedImage?): MutableList<DoubleArray> {
            var widthList = mutableListOf<DoubleArray>()
            for (i in 0 until polygon.size step 2) {
                val p0 = polygon[Math.max(i - 1, 0)]
                val p1 = polygon[i]
                val p2 = polygon[Math.min(i + 1, polygon.size - 1)]

                val widthForPoints = doubleArrayOf(
                        // Use the inverted brightness as width of the line we drawSpline.
                        (1 - ColorUtils.getLightnessFromImage(p0.first, p0.second, lightnessImage)),
                        (1 - ColorUtils.getLightnessFromImage(p1.first, p1.second, lightnessImage)),
                        (1 - ColorUtils.getLightnessFromImage(p2.first, p2.second, lightnessImage))
                )
                widthList = (widthList + widthForPoints).toMutableList()
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

        private fun drawSpline(g2: Graphics2D,
                               polygonPoints: DoubleArray,
                               widthForPoints: DoubleArray,
                               colors: List<Color>,
                               sidePadding: Double,
                               size: Double,
                               lineWidth: Double) {


            val euclideanDistance = Math.sqrt(
                    Math.abs(Math.pow(polygonPoints[0] - polygonPoints[2], 2.0) +
                            Math.pow(polygonPoints[1] - polygonPoints[3], 2.0)))

            //TODO dela upp i många små steg, låt stegen vara propotionella mot width/3?
            // räkna euclidiskt avstånd mellan xy0 och xy2, använd ihop med width för att stega frammåt i lagom stora steg
            var t = 0.0
            while (t <= 1.0) {
                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

                // Calculate the t value used in the Bezier calculations below.

                // Calculate the Bezier (x, y) coordinate for this step.
                val x = (Math.pow((1 - t), 2.0) * polygonPoints[0]) +
                        (2 * (1 - t) * t * polygonPoints[2]) +
                        (Math.pow(t, 2.0) * polygonPoints[4])
                val y = (Math.pow((1 - t), 2.0) * polygonPoints[1]) +
                        (2 * (1 - t) * t * polygonPoints[3]) +
                        (Math.pow(t, 2.0) * polygonPoints[5])

                // Calculate the Bezier width for this step.
                val width = ((Math.pow((1 - t), 2.0) * widthForPoints[0]) +
                        (2 * (1 - t) * t * widthForPoints[1]) +
                        (Math.pow(t, 2.0) * widthForPoints[2])) * lineWidth
                //print("x " + x * size + ", y " + y * size + ", w " + width + " ds: " + drawSteps + "\n")

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

                t += 0.1
            }
            val plotDbugPoints = false
            if (plotDbugPoints) {
                if (euclideanDistance > 0.02) {
                    g2.color = Color.BLACK
                } else {
                    g2.color = Color.CYAN

                }
                g2.fill(Ellipse2D.Double(
                        ((polygonPoints[0] * size) - 2.5) + sidePadding,
                        ((polygonPoints[1] * size) - 2.5) + sidePadding,
                        5.0, 4.0))
                g2.fill(Ellipse2D.Double(
                        ((polygonPoints[2] * size) - 2.5) + sidePadding,
                        ((polygonPoints[3] * size) - 2.5) + sidePadding,
                        4.0, 5.0))

//            g2.color = Color.GREEN
//            g2.fill(Ellipse2D.Double(
//                    ((polygonPoints[4] * size) - 2.5) + sidePadding,
//                    ((polygonPoints[5] * size) -  2.5) + sidePadding,
//                    5.0, 5.0))
            }
        }
    }
}