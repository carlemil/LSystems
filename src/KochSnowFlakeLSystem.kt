class kochSnowFlakeLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return Math.PI / 2.0
    }

    override fun getName(): String {
        return "KochSnowFlakeLSystem"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'F' to "F-F+F+FF-F-F+F",
                '+' to "+",
                '-' to "-")
    }

    override fun getAxiom(): String {
        return "F"
    }
}