package lSystem

class LSystemDefinition(val name: String,
                        val angle: Double,
                        val maxIterations: Int,
                        val rules: Map<String, String>,
                        val forwardChars: Set<String>,
                        val axiom: String)