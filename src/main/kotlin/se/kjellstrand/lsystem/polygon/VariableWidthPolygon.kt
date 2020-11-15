package se.kjellstrand.lsystem.polygon

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import kotlin.math.*

class VariableWidthPolygon {

    companion object {
        fun drawPolygonToBufferedImage(polygon: List<PolygonPoint>,
                                       g2: Graphics2D,
                                       size: Double,
                                       sidePadding: Double) {
            val t0 = System.currentTimeMillis()

            val hull = buildHullFromPolygon(polygon, size)
            val t1 = System.currentTimeMillis()
            print("Build polygon: " + (t1 - t0) + "ms\n")

            drawPolygon(hull, g2, sidePadding)
            val t2 = System.currentTimeMillis()
            print("Draw polygon: " + (t2 - t1) + "ms\n")
        }

        fun calculateSidesOfTriangle(p0: PolygonPoint, p1: PolygonPoint): Triple<Double, Double, Double> {
            val a = p0.x - p1.x
            val b = p0.y - p1.y
            val c = sqrt(a.pow(2.0) + b.pow(2.0))
            return Triple(a, b, c)
        }

        fun tearDownGraphics(g2: Graphics2D) {
            g2.dispose()
        }

        fun drawDebugPolygonPoints(polygon: List<PolygonPoint>,
                                   g2: Graphics2D,
                                   size: Double,
                                   sidePadding: Double) {
            g2.paint = Color(1f, 0f, 0f, .3f)
            val width = 15
            polygon.forEach { p ->
                g2.fillOval((p.x * size).toInt(), (p.y * size).toInt(), width, width)
            }
            val p = polygon[polygon.size - 1]
            g2.paint = Color(0f, 1f, 0f, .7f)
            g2.fillOval((p.x * size).toInt(), (p.y * size).toInt(), width, width)
        }

        private fun buildHullFromPolygon(ppList: List<PolygonPoint>, size: Double): MutableList<PolygonPoint> {
            val leftHull = mutableListOf<PolygonPoint>()
            val rightHull = mutableListOf<PolygonPoint>()

            for (i in 1 until ppList.size) {
                val p0 = ppList[i - 1]
                val p1 = ppList[i]

                val (a, b, c) = calculateSidesOfTriangle(p0, p1)

                val alfaPlus90 = calculateAlfa(a, c, b, (PI / 2.0))
                val alfaMinus90 = calculateAlfa(a, c, b, -(PI / 2.0))

                leftHull.add(calculatePerpendicularPolyPoint(p0, p1, size, alfaPlus90))
                rightHull.add(calculatePerpendicularPolyPoint(p0, p1, size, alfaMinus90))
            }

            val hull = mutableListOf<PolygonPoint>()
            hull.addAll(leftHull)
            hull.addAll(rightHull.reversed())
            return hull
        }

        private fun drawPolygon(hull: MutableList<PolygonPoint>, g2: Graphics2D, sidePadding: Double) {
            val path = GeneralPath()
            val polygonInitialPP = PolygonPoint.average(hull[hull.size - 1], hull[hull.size - 2])
            path.moveTo(polygonInitialPP.x, polygonInitialPP.y)

            for (i in 0 until hull.size) {
                val quadStartPP = hull[(if (i == 0) hull.size else i) - 1]
                val nextQuadStartPP = hull[i]
                val quadEndPP = PolygonPoint.average(quadStartPP, nextQuadStartPP)
                path.quadTo(quadStartPP.x, quadStartPP.y, quadEndPP.x, quadEndPP.y)
            }

            g2.paint = Color.BLACK

            path.closePath()
            val at = AffineTransform()
            at.translate(sidePadding, sidePadding)
            path.transform(at)
            g2.fill(path)
        }

        private fun calculatePerpendicularPolyPoint(p0: PolygonPoint, p1: PolygonPoint, size: Double, alfaPlus90: Double): PolygonPoint {
            val (_, _, c) = calculateSidesOfTriangle(p0, p1)
            // * 0.75 (0.75 + 0.10 == 0.85) to set a max width that is close to touching the nearest line.
            // + 0.10 to set a min width that is still visible.
            // * 3 since we use 3 intermediate points when we generate the polygon (the smooth step adds them)
            val width = (size * c * (((p0.w + p1.w) / 2) * 0.75 + 0.10)) / 2
            val x = (p0.x + p1.x) * size / 2.0 + width * sin(alfaPlus90)
            val y = (p0.y + p1.y) * size / 2.0 + width * cos(alfaPlus90)
            return PolygonPoint(x, y)
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