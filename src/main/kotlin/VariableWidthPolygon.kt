import lSystem.PolyPoint
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import kotlin.math.*

class VariableWidthPolygon {

    companion object {
        fun drawPolygonToBufferedImage(polygon: List<PolyPoint>,
                                       g2: Graphics2D,
                                       size: Double,
                                       sidePadding: Double) {
            val t0 = System.currentTimeMillis()

            val hull = buildPolygon(polygon, size)
            val t1 = System.currentTimeMillis()
            print("Build polygon: " + (t1 - t0) + "ms\n")

            drawPolygon(hull, g2, sidePadding)
            val t2 = System.currentTimeMillis()
            print("Draw polygon: " + (t2 - t1) + "ms\n")

            tearDownGraphics(g2)
            val t3 = System.currentTimeMillis()
            print("Tear down graphics: " + (t3 - t2) + "ms\n")
        }

        fun drawDebugPolygon(polygon: List<PolyPoint>,
                             g2: Graphics2D,
                             size: Double,
                             sidePadding: Double) {
            val c = Color(1f, 0f, 0f, .3f)
            g2.paint =c
            val width = 15
            polygon.forEach { polypoint ->
                val x = polypoint.x
                val y = polypoint.y
                g2.fillOval((x * size).toInt(), (y * size).toInt(), width, width)
            }

        }

        fun calculateSidesOfTriangle(p0: PolyPoint, p1: PolyPoint): Triple<Double, Double, Double> {
            val a = p0.x - p1.x
            val b = p0.y - p1.y
            val c = sqrt(a.pow(2.0) + b.pow(2.0))
            return Triple(a, b, c)
        }

        private fun tearDownGraphics(g2: Graphics2D) {
            g2.dispose()
        }

        private fun buildPolygon(ppList: List<PolyPoint>, size: Double): MutableList<PolyPoint> {
            val leftHull = mutableListOf<PolyPoint>()
            val rightHull = mutableListOf<PolyPoint>()

            for (i in 1 until ppList.size) {
                val p0 = ppList[i - 1]
                val p1 = ppList[i]

                val (a, b, c) = calculateSidesOfTriangle(p0, p1)

                val alfaPlus90 = calculateAlfa(a, c, b, (PI / 2.0))
                val alfaMinus90 = calculateAlfa(a, c, b, -(PI / 2.0))

                leftHull.add(calculatePerpendicularPolyPoint(p0, p1, size, alfaPlus90))
                rightHull.add(calculatePerpendicularPolyPoint(p0, p1, size, alfaMinus90))
            }

            val hull = mutableListOf<PolyPoint>()
            hull.addAll(leftHull)
            hull.addAll(rightHull.reversed())
            return hull
        }

        private fun drawPolygon(hull: MutableList<PolyPoint>, g2: Graphics2D, sidePadding: Double) {
            val path = GeneralPath()
            val polygonInitialPP = PolyPoint.average(hull[hull.size - 1], hull[hull.size - 2])
            path.moveTo(polygonInitialPP.x, polygonInitialPP.y)

            for (i in 0 until hull.size) {
                val quadStartPP = hull[(if (i == 0) hull.size else i) - 1]
                val nextQuadStartPP = hull[i]
                val quadEndPP = PolyPoint.average(quadStartPP, nextQuadStartPP)
                path.quadTo(quadStartPP.x, quadStartPP.y, quadEndPP.x, quadEndPP.y)
            }

            g2.paint = Color.BLACK

            path.closePath()
            val at = AffineTransform()
            at.translate(sidePadding, sidePadding)
            path.transform(at)
            g2.fill(path)
        }

        private fun calculatePerpendicularPolyPoint(p0: PolyPoint, p1: PolyPoint, size: Double, alfaPlus90: Double): PolyPoint {
            val (_, _, c) = calculateSidesOfTriangle(p0, p1)
            // * 0.75 (0.75 + 0.10 == 0.85) to set a max width that is close to touching the nearest line.
            // + 0.10 to set a min width that is still visible.
            // * 3 since we use 3 intermediate points when we generate the polygon (the smooth step adds them)
            val width = (size * c *( ((p0.w + p1.w) / 2) * 0.75 + 0.10)) / 2 * 3
            val x = (p0.x + p1.x) * size / 2.0 + width * sin(alfaPlus90)
            val y = (p0.y + p1.y) * size / 2.0 + width * cos(alfaPlus90)
            return PolyPoint(x, y)
        }

        private fun calculateAlfa(a: Double, c: Double, b: Double, angle: Double): Double {
            val alfa = asin(a / c)
            var alfaPlus90 = alfa + angle
            // Take care of special case when adjacent side is negative
            if (b < 0) alfaPlus90 = -alfaPlus90
            return alfaPlus90
        }
    }
}