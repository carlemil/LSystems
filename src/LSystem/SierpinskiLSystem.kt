package LSystem

class sierpinskiLSystem : LSystem.LSystem.LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return Math.PI / 3
    }

    override fun getName(): String {
        return "SierpinskiCurve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "BF-AF-B",
                'B' to "AF+BF+A",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "A"
    }
}

