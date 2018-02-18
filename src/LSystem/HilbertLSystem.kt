package LSystem

class hilbertLSystem : LSystem.LSystem.LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return Math.PI / 2
    }

    override fun getName(): String {
        return "HilbertCurve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "-BF+AFA+FB-",
                'B' to "+AF-BFB-FA+",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "A"
    }
}