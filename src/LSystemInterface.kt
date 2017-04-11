/**
 * Created by carlemil on 4/11/17.
 */

interface LSystem {
    fun getAxiom(): String
    fun getName(): String
    fun getRules(): Map<Char, String>
}