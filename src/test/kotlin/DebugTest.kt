import org.junit.Test

class DebugTest {

    @Test
    internal fun debugSomething() {

        for(i in -127..128 step 10) {
            var b : Byte = i.toByte()
            println("i: " + b + " : " + ((b).toFloat() / 255f + 0.5f))
        }
    }
}