package se.kjellstrand.lsystem.model

import kotlin.math.PI

class LSystem(
    val name: String,
    val angle: Double,
    val rules: Map<String, String>,
    val forwardChars: Set<String>,
    val axiom: String,
    val lineWidthExp: Double,
    val lineWidthBold: Double
) {
    fun getAngleInRadians(): Float {
        return (angle / 180 * PI).toFloat()
    }

    companion object {

        fun getByName(name: String): LSystem? {
            return this.systems.find { lsd -> lsd.name.startsWith(name, true) }
        }

        val systems: List<LSystem> = listOf(
            LSystem(
                name = "KochSnowFlake",
                angle = 90.0,
                rules = mapOf("F" to "F-F+F+FF-F-F+F"),
                forwardChars = setOf("F"),
                axiom = "F",
                lineWidthExp = 3.3,
                lineWidthBold = 1.0
            ),
            LSystem(
                name = "Dragon",
                angle = 90.0,
                rules = mapOf("A" to "A+BF", "B" to "FA-B"),
                forwardChars = setOf("F"),
                axiom = "FA",
                lineWidthExp = 2.4,
                lineWidthBold = 1.0
            ),
            LSystem(
                name = "TwinDragon",
                angle = 90.0,
                rules = mapOf("A" to "A+BF", "B" to "FA-B"),
                forwardChars = setOf("F"),
                axiom = "FA+FA+",
                lineWidthExp = 2.4,
                lineWidthBold = 1.0
            ),
            LSystem(
                name = "Fudgeflake",
                angle = 30.0,
                rules = mapOf("F" to "+F----F++++F-"),
                forwardChars = setOf("F"),
                axiom = "F++++F++++F",
                lineWidthExp = 3.0,
                lineWidthBold = 1.5
            ),
            LSystem(
                name = "Hilbert",
                angle = 90.0,
                rules = mapOf("A" to "-BF+AFA+FB-", "B" to "+AF-BFB-FA+"),
                forwardChars = setOf("F"),
                axiom = "A",
                lineWidthExp = 4.0,
                lineWidthBold = 16.0
            ),
            LSystem(
                name = "SierpinskiTriangle",
                angle = 60.0,
                rules = mapOf("A" to "BF-AF-B", "B" to "AF+BF+A"),
                forwardChars = setOf("F"),
                axiom = "A",
                lineWidthExp = 4.6,
                lineWidthBold = 40.0
            ),
            LSystem(
                name = "SierpinskiCurve",
                angle = 45.0,
                rules = mapOf("X" to "XF+G+XF--F--XF+G+X"),
                forwardChars = setOf("F", "G"),
                axiom = "F--XF--F--XF",
                lineWidthExp = 3.34,
                lineWidthBold = 2.0
            ),
            LSystem(
                name = "SierpinskiSquare",
                angle = 90.0,
                rules = mapOf("X" to "XF-F+F-XF+F+XF-F+F-X"),
                forwardChars = setOf("F"),
                axiom = "F+XF+F+XF",
                lineWidthExp = 3.4,
                lineWidthBold = 2.0
            ),
            LSystem(
                name = "Gosper",
                angle = 60.0,
                rules = mapOf("A" to "A-B--B+A++AA+B-", "B" to "+A-BB--B-A++A+B"),
                forwardChars = setOf("A", "B"),
                axiom = "A",
                lineWidthExp = 4.5,
                lineWidthBold = 8.0
            ),
            LSystem(
                name = "Peano",
                angle = 90.0,
                rules = mapOf("L" to "LFRFL-F-RFLFR+F+LFRFL", "R" to "RFLFR+F+LFRFL-F-RFLFR"),
                forwardChars = setOf("F"),
                axiom = "L",
                lineWidthExp = 4.9,
                lineWidthBold = 16.0
            ),
            LSystem(
                name = "Moore",
                angle = 90.0,
                rules = mapOf("A" to "-BF+AFA+FB-", "B" to "+AF-BFB-FA+"),
                forwardChars = setOf("F"),
                axiom = "AFA+F+AFA",
                lineWidthExp = 3.8,
                lineWidthBold = 8.0
            )
        )
    }
}