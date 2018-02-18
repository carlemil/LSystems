class dragonLSystem : LSystem {
    override fun getForwardChars(): Set<Char> {
        return setOf('F')
    }

    override fun getAngle(): Double {
        return Math.PI / 2.0
    }

    override fun getName(): String {
        return "DragonCurve"
    }

    override fun getRules(): Map<Char, String> {
        return mapOf(
                'A' to "A+BF",
                'B' to "FA-B",
                '+' to "+",
                '-' to "-",
                'F' to "F")
    }

    override fun getAxiom(): String {
        return "FA"
    }
}