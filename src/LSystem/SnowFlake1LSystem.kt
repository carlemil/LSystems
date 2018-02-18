package LSystem

class snowFlake1LSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('A', 'B')
    }

    override fun getAngle(): Double {
        return Math.PI / 3.0
    }

    override fun getName(): String {
        return "SnowFlake1Curve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "A-B--B+A++AA+B-",
                'B' to "+A-BB--B-A++A+B",
                '+' to "+",
                '-' to "-")
    }

    override fun getAxiom(): String {
        return "A"
    }
}