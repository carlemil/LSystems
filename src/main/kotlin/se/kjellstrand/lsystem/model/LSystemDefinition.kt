package se.kjellstrand.lsystem.model

import kotlin.math.PI

class LSystemDefinition(
    val name: String,
    val angle: Double,
    val rules: Map<String, String>,
    val forwardChars: Set<String>,
    val axiom: String,
    val lineWidthExp: Double,
    val lineWidthBold: Double
) {

    fun getAngleInRadians(): Double {
        return angle / 180 * PI
    }
}