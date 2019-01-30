package LSystem

class LSystemDefinition(val name: String,
                        val angle: Double,
                        val scaling: Double,
                        val rules: Map<String, String>,
                        val forwardChars: Set<String>,
                        val axiom: String)