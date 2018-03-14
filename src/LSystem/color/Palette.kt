package LSystem.color

object Palette {

    fun getPalette(theme: Theme, length: Int, brightness: Int): IntArray {

        adjustBrightness(theme.palette, brightness)

        val palette = IntArray(length)

        if (theme.drawMode == DrawMode.ZEBRA) {
            setFlagBands(palette, theme.palette)
        } else if (theme.drawMode == DrawMode.ZEBRA_GRADIENT) {
            setGradient(palette, theme.palette, theme.blendMode)
            zebraify(palette)
        } else if (theme.drawMode == DrawMode.GRADIENT) {
            setGradient(palette, theme.palette, theme.blendMode)
        }

        return palette
    }

    private fun adjustBrightness(colors: IntArray, brightness: Int) {
        for (i in colors.indices) {
            val f3 = rgbToFloat3(colors[i])
            for (j in 0..2) {
                f3[j] = f3[j] * (brightness / 100f)
            }
            colors[i] = float3ToInt(f3)
        }
    }

    private fun setGradient(palette: IntArray, colors: IntArray, blendMode: BlendMode) {
        for (i in 1 until colors.size) {
            val pl = palette.size
            val cl = colors.size - 1
            val i1 = Math.round(pl / cl.toFloat() * (i - 1))
            val i2 = Math.round(pl / cl.toFloat() * i)
            val c1 = colors[i - 1]
            val c2 = colors[i]
            val p = IntArray(i2 - i1)
            setGradient(p, c1, c2, blendMode)
            System.arraycopy(p, 0, palette, i1, p.size)
        }
    }

    private fun zebraify(palette: IntArray) {
        for (i in palette.indices) {
            if (i % 2 == 1) {
                val c = palette[i]
                val r = c and 0xff0000 shr 1 and 0xff0000
                val g = c and 0x00ff00 shr 1 and 0x00ff00
                val b = c and 0x0000ff shr 1 and 0x0000ff
                palette[i] = r + g + b
            }
        }
    }

    private fun byteify(palette: IntArray): ByteArray {
        val bytePalette = ByteArray(palette.size * 3)
        for (i in palette.indices) {
            bytePalette[i * 3 + 2] = (palette[i] shr 16 and 0xff).toByte()
            bytePalette[i * 3 + 1] = (palette[i] shr 8 and 0xff).toByte()
            bytePalette[i * 3 + 0] = (palette[i] shr 0 and 0xff).toByte()
        }
        return bytePalette
    }

    private fun setFlagBands(palette: IntArray, colors: IntArray) {
        for (i in palette.indices) {
            palette[i] = colors[i % colors.size]
        }
    }

    private fun setGradient(palette: IntArray, startColor: Int, endColor: Int, blendMode: BlendMode) {
        var start = FloatArray(3)
        var end = FloatArray(3)
        val tmp = FloatArray(3)

        //        if (blendMode == BlendMode.HSV) {
        //            Color.colorToHSV(startColor, start);
        //            Color.colorToHSV(endColor, end);
        //        } else {
        start = rgbToFloat3(startColor)
        end = rgbToFloat3(endColor)
        //        }

        for (i in palette.indices) {
            val p = i.toFloat() / palette.size
            for (j in 0..2) {
                tmp[j] = start[j] * (1f - p) + end[j] * p
            }
            //            if (blendMode == BlendMode.HSV) {
            //                palette[i] = Color.HSVToColor(tmp);
            //            } else {
            palette[i] = float3ToInt(tmp)
            //            }
        }
    }

    private fun float3ToInt(f3: FloatArray): Int {
        return f3[0].toInt() + (f3[1].toInt() shl 8) + (f3[2].toInt() shl 16)
    }

    fun rgbToFloat3(color: Int): FloatArray {
        val f3 = FloatArray(3)
        f3[0] = (color and 255).toFloat()
        f3[1] = (color shr 8 and 255).toFloat()
        f3[2] = (color shr 16 and 255).toFloat()
        return f3
    }

}
