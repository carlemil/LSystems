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

            drawThePolygonOutline(g2, polygonDoubleArrayList, size, polygon, widthListForPolygon, sidePadding, lineWidth)

            val t3 = System.currentTimeMillis()
            print("Draw spline outlines: " + (t3 - t2) + "ms\n")

            drawThePolygon(lineWidth, outlineWidth, polygonDoubleArrayList, hueImage, g2, size, polygon, widthListForPolygon, sidePadding)
            g2.dispose()

            val t4 = System.currentTimeMillis()
            print("Draw splines: " + (t4 - t3) + "ms\n")
            print("Draw total: " + (t4 - t0) + "ms\n")

            return bufferedImage
        }

        private fun drawThePolygonOutline(g2: Graphics2D, allPolygonPoints: MutableList<DoubleArray>, size: Double, polygon: List<Pair<Double, Double>>, allWidthForPoints: MutableList<DoubleArray>, sidePadding: Double, lineWidth: Double) {
            g2.paint = Color.BLACK
            for (i in 0 until allPolygonPoints.size) {
                drawSpline(g2, (size / Math.sqrt(polygon.size.toDouble())).toInt(),
                        allPolygonPoints[i], allWidthForPoints[i], sidePadding, size, lineWidth)
            }
        }

        private fun drawThePolygon(lineWidth: Double, outlineWidth: Double, allPolygonPoints: MutableList<DoubleArray>,
                                   hueImage: BufferedImage?, g2: Graphics2D, size: Double, polygon: List<Pair<Double, Double>>,
                                   allWidthForPoints: MutableList<DoubleArray>, sidePadding: Double) {
            val width = lineWidth - outlineWidth
            if (width > 0) {
                for (i in 0 until allPolygonPoints.size) {
                    if (hueImage != null) {
                        g2.paint = ColorUtils.getColorFromImage(
                                allPolygonPoints[i][0],
                                allPolygonPoints[i][1],
                                hueImage)
                    } else {
                        g2.paint = Color.ORANGE
                    }
                    drawSpline(g2, (size / Math.sqrt(polygon.size.toDouble())).toInt(),
                            allPolygonPoints[i], allWidthForPoints[i], sidePadding, size, width)
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
                               drawSteps: Int,
                               polygonPoints: DoubleArray,
                               widthForPoints: DoubleArray,
                               sidePadding: Double,
                               size: Double,
                               lineWidth: Double) {
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

                val width = ((Math.pow((1 - t), 2.0) * widthForPoints[0]) +
                        (2 * (1 - t) * t * widthForPoints[1]) +
                        (Math.pow(t, 2.0) * widthForPoints[2])) * lineWidth

                val circle = Ellipse2D.Double(
                        ((x * size) - width / 2) + sidePadding,
                        ((y * size) - width / 2) + sidePadding,
                        width, width)

                g2.fill(circle)
            }
        }

//        private fun getBrightnessFromImage(inX: Double, inY: Double, image: BufferedImage?): Double {
//            var color = 0xffffff
//            if (image != null) {
//                val x = (inX * (image.width - 1.0)).toInt()
//                val y = (inY * (image.height - 1.0)).toInt()
//                color = image.getRGB(x, y)
//            }
//            var c = FloatArray(3)
//            Color.RGBtoHSB(
//                    color shr 16 and 255,
//                    color shr 8 and 255,
//                    color and 255,
//                    c)
//            return c[2].toDouble()
//        }
    }
}