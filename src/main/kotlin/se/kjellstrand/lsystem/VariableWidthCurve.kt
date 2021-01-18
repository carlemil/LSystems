package se.kjellstrand.lsystem

import se.kjellstrand.lsystem.model.LSTriple
import kotlin.math.*

fun buildHullFromPolygon(ppList: List<LSTriple>): MutableList<LSTriple> {
    val leftHull = mutableListOf<LSTriple>()
    val rightHull = mutableListOf<LSTriple>()

    for (i in 1 until ppList.size) {
        val p0 = ppList[i - 1]
        val p1 = ppList[i]

        val (a, b, c) = calculateSidesOfTriangle(p0, p1)

        val alfaPlus90 = calculateAlfa(a, c, b, (PI / 2.0).toFloat())
        val alfaMinus90 = calculateAlfa(a, c, b, -(PI / 2.0).toFloat())

        leftHull.add(calculatePerpendicularPolyPoint(p0, p1, alfaPlus90))
        rightHull.add(calculatePerpendicularPolyPoint(p0, p1, alfaMinus90))
    }

    val hull = mutableListOf<LSTriple>()
    hull.addAll(leftHull)
    hull.addAll(rightHull.reversed())
    return hull
}

private fun calculatePerpendicularPolyPoint(
    p0: LSTriple,
    p1: LSTriple,
    alfaPlus90: Float
): LSTriple {
    val width = ((p0.w + p1.w) / 2.0)
    val x = (p0.x + p1.x) / 2.0 + width * sin(alfaPlus90)
    val y = (p0.y + p1.y) / 2.0 + width * cos(alfaPlus90)
    return LSTriple(x.toFloat(), y.toFloat(), 1.0F)
}

private fun calculateSidesOfTriangle(
    p0: LSTriple,
    p1: LSTriple
): Triple<Float,Float,Float>{
    val a = p0.x - p1.x
    val b = p0.y - p1.y
    val c = sqrt(a.toDouble().pow(2.0) + b.toDouble().pow(2.0)).toFloat()
    return Triple(a, b, c)
}

private fun calculateAlfa(a: Float, c: Float, b: Float, angle: Float): Float {
    val alfa = asin(a / c)
    var alfaPlus90 = alfa + angle
    // Take care of special case when adjacent side is negative
    if (b < 0) alfaPlus90 = -alfaPlus90
    return alfaPlus90
}

fun getMidPoint(
    p0: LSTriple,
    p1: LSTriple
): LSTriple {
    return LSTriple((p0.x + p1.x) / 2.0F, (p0.y + p1.y) / 2.0F, (p0.w + p1.w) / 2.0F)
}