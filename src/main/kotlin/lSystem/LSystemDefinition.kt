package lSystem

import kotlin.math.PI

class LSystemDefinition(val name: String,
                        val angle: Double,
                        val maxIterations: Int,
                        val rules: Map<String, String>,
                        val forwardChars: Set<String>,
                        val axiom: String) {

    fun getAngleInRadians(): Double {
        return angle / 180 * PI
    }
}