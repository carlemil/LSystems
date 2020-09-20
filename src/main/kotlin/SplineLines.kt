import lSystem.PolyPoint
import java.awt.*
import java.awt.RenderingHints.*
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.*


class SplineLines {

    companion object {

        private val image = getRenderedCircle()

        fun drawPolygonAsSplines(polygon: List<PolyPoint>,
                                 hueImage: BufferedImage?,
                                 brightnessImage: BufferedImage?,
                                 size: Double,
                                 lineWidth: Double,
                                 sidePadding: Double): BufferedImage {

            val t0 = System.currentTimeMillis()

            val (bufferedImage, g2) = setupGraphics(size, sidePadding)

            val polygonWithWidthAdjusted = adjustWidthAccordingToImage(polygon, brightnessImage)
            val polygonWithColor = setColorToPolygon(polygonWithWidthAdjusted, hueImage)

            val t1 = System.currentTimeMillis()
            print("Prepare data for drawing: " + (t1 - t0) + "ms\n")

            drawThePolygon(g2, polygonWithColor, hueImage, size, lineWidth, sidePadding)

            val t2 = System.currentTimeMillis()
            print("Draw splines: " + (t2 - t1) + "ms\n")

            trig(g2, polygonWithColor, size, lineWidth, sidePadding)

            val t3 = System.currentTimeMillis()
            print("Draw polygon: " + (t3 - t2) + "ms\n")

            // No drawing can be performed after this.
            g2.dispose()

            return bufferedImage
        }

        private fun trig(g2: Graphics2D, ppList: List<PolyPoint>, size: Double, lineWidth: Double, sidePadding: Double) {
            val leftHull = mutableListOf<PolyPoint>()
            val rightHull = mutableListOf<PolyPoint>()

            for (i in 1 until ppList.size) {
                val p0 = ppList[i - 1]
                val p1 = ppList[i]

                val a = p0.x - p1.x
                val b = p0.y - p1.y
                val c = sqrt(a.pow(2.0) + b.pow(2.0))

                var alfaPlus90 = calculateAlfa(a, c, b, (PI / 2.0))
                var alfaMinus90 = calculateAlfa(a, c, b, -(PI / 2.0))


                val leftPPPP = calculatePerpendicularPolyPoint(p0, p1, size, lineWidth, alfaPlus90)
                leftHull.add(leftPPPP)

                val rightPPPP = calculatePerpendicularPolyPoint(p0, p1, size, lineWidth, alfaMinus90)
                rightHull.add(rightPPPP)
            }

            val hull = mutableListOf<PolyPoint>()
            hull.addAll(leftHull)
            hull.addAll(rightHull.reversed())

            val path = GeneralPath()
            //path.quadTo(205f, 250f, 340f, 300f)

            path.moveTo(ppList[0].x + sidePadding, ppList[0].y + sidePadding)
            for (pp in hull) {
                path.lineTo(pp.x + sidePadding, pp.y + sidePadding)
            }
            g2.paint = Color.RED
            path.closePath()
            g2.fill(path)
        }

        private fun calculatePerpendicularPolyPoint(p0: PolyPoint, p1: PolyPoint, size: Double, lineWidth: Double, alfaPlus90: Double): PolyPoint {
            val width = (p0.w + p1.w) / 2
            val x = (p0.x + p1.x) * size / 2.0 + lineWidth * width * sin(alfaPlus90)
            val y = (p0.y + p1.y) * size / 2.0 + lineWidth * width * cos(alfaPlus90)
            return PolyPoint(x, y)
        }

        private fun calculateAlfa(a: Double, c: Double, b: Double, angle: Double): Double {
            val alfa = asin(a / c)
            var alfaPlus90 = alfa + angle
            // Take care of special case when adjacent side is negative
            if (b < 0) alfaPlus90 = -alfaPlus90
            return alfaPlus90
        }

        private fun createImage(size: Int): BufferedImage {
            val buffImg = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE)
            val gbi = buffImg.createGraphics()
            gbi.background = Color(255, 255, 0, 0);
            gbi.clearRect(0, 0, buffImg.width, buffImg.height)
            val rh = mutableMapOf<Key, Any>()
            rh[KEY_ANTIALIASING] = VALUE_ANTIALIAS_ON
            rh[KEY_ALPHA_INTERPOLATION] = VALUE_ALPHA_INTERPOLATION_QUALITY
            rh[KEY_COLOR_RENDERING] = VALUE_COLOR_RENDER_QUALITY
            rh[KEY_RENDERING] = VALUE_RENDER_QUALITY
            rh[KEY_STROKE_CONTROL] = VALUE_STROKE_PURE
            gbi.setRenderingHints(rh)
            gbi.paint = Color.BLACK
            gbi.fillOval(0, 0, buffImg.width, buffImg.height)
            return buffImg
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

            for (i in pp.indices) {
                // Get 3 points and set color to them
                var tmp0 = pp[Math.max(i - 1, 0)]
                val p0 = PolyPoint(tmp0.x, tmp0.y, tmp0.w, ColorUtils.getColorFromImage(tmp0.x, tmp0.y, hueImage))

                var tmp1 = pp[i]
                val p1 = PolyPoint(tmp1.x, tmp1.y, tmp1.w, ColorUtils.getColorFromImage(tmp1.x, tmp1.y, hueImage))

                val tmp2 = pp[Math.min(i + 1, pp.size - 1)]
                val p2 = PolyPoint(tmp2.x, tmp2.y, tmp2.w, ColorUtils.getColorFromImage(tmp2.x, tmp2.y, hueImage))

                // Calculate intermediate draw points
                val dp0 = PolyPoint.average(p0, p1)
                val dp2 = PolyPoint.average(p1, p2)

                // Draw spline segment
                drawSpline(g2, dp0, p1, dp2, size, lineWidth, sidePadding)
            }
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

            val euclideanDistance = sqrt(
                    abs((pp1.x - pp2.x).pow(2.0) +
                            (pp1.y - pp2.y).pow(2.0)))

            var t = 0.0
            while (t <= 1.0) {
                // The bezier square spline is calculated using this formula from wikipedia.
                // P = pow2(1−t)*P1 + 2(1−t)t*P2 + pow2(t)*P3

                // Calculate the Bezier (x, y) coordinate for this step.
                val x = ((1 - t).pow(2.0) * pp1.x) +
                        (2 * (1 - t) * t * pp2.x) +
                        (t.pow(2.0) * pp3.x)
                val y = ((1 - t).pow(2.0) * pp1.y) +
                        (2 * (1 - t) * t * pp2.y) +
                        (t.pow(2.0) * pp3.y)

                // Calculate the Bezier width for this step.
                val width = 1.0.coerceAtLeast((((1 - t).pow(2.0) * pp1.w) +
                        (2 * (1 - t) * t * pp2.w) +
                        (t.pow(2.0) * pp3.w)) * lineWidth)

                // Crate a circle at the right spot and size
//                val circle = Rectangle2D.Double(
//                        ((x * size) - width / 2) + sidePadding,
//                        ((y * size) - width / 2) + sidePadding,
//                        width, width)

                // Calculate the color of the circle for this step.
                var color: Color = when {
                    t < 0.5 -> ColorUtils.blend(pp1.c, pp2.c, 1.0 - t * 2)
                    else -> ColorUtils.blend(pp2.c, pp3.c, 1.0 - (t - 0.5) * 2)
                }

                // Set the color of the circle.
                g2.color = color

                // Draw the circle.
                // g2.fill(circle)

                g2.drawImage(image[width.toInt()],
                        (((x * size) - width / 2) + sidePadding).toInt(),
                        (((y * size) - width / 2) + sidePadding).toInt(),
                        null
                )

                // Calculate the t value used in the Bezier calculations above.
                t += (width / 2.0) / (Math.max(1.0, euclideanDistance) * size)
            }
        }

        // For 4 control points: P = (1−t)3P1 + 3(1−t)2tP2 +3(1−t)t2P3 + t3P4

        private fun getRenderedCircle(): ArrayList<BufferedImage> {
            val arrayList = arrayListOf<BufferedImage>()
            for (i in 1..1000) {
                arrayList.add(createImage(i))
            }
            return arrayList
        }

    }
}