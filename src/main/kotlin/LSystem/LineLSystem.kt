package LSystem

class lineLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return Math.PI/2.0
    }

    override fun getName(): String {
        return "Line"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                '+' to "+",
                '-' to "-",
                'F' to "F",
                'A' to "AF+F-F-F+")
    }

    override fun getAxiom(): String {
        return "A"
    }
}

