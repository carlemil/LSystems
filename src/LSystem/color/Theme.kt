package LSystem.color

class Theme(themeName: String) {
    var blendMode = BlendMode.RGB

    var drawMode = DrawMode.GRADIENT

    var palette = intArrayOf(0xffffff, 0x000000)

    init {
        when (themeName) {
            "montage" -> {
                palette = intArrayOf(0xF8E8D5, 0xB1DDF3, 0xFFDE89, 0xC2D985,0xE9EBF0, 0xF1B2E1)
                blendMode = BlendMode.HSV
                drawMode = DrawMode.ZEBRA_GRADIENT
            }
            "black_n_white" -> drawMode = DrawMode.GRADIENT
            "bee_stripes" -> {
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0xffff00, 0x222200)
            }
            "blue_hole" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0xffffff, 0x0000ff, 0x000022)
            }
            "blue_yellow" -> {
                blendMode = BlendMode.HSV
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0x00ddff, 0xfeed00)
            }
            "bright_pink" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0xff0096, 0xffffff)
            }
            "pale_pink" -> {
                palette = intArrayOf(0xfadadd, 0xd4999e, 0xae636a, 0x873940, 0xae636a, 0xd4999e)
            }
            "red_white" -> {
                palette = intArrayOf(0xffffff, 0xed2939)
            }
            "sunrise" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x0068ff, 0xb4a2cc, 0xe0af22, 0xffb628, 0xfffc46, 0xffffaf)
            }
            "shiny_scales" -> {
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0x00f0ba, 0xd1857e, 0x502065, 0x17b4c7, 0x7315b7)
            }
            "slime_green" -> {
                blendMode = BlendMode.HSV
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x8efc00, 0x779d00)
            }
            "sunset" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x000000, 0x422343, 0xaa5167, 0xf06659, 0xf19516, 0xfff312)
            }
            "milky_way" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff)
            }
            "teal" -> {
                drawMode = DrawMode.GRADIENT
                blendMode = BlendMode.HSV
                palette = intArrayOf(0x00a0a0, 0x002020)
            }
            "terracotta" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x6f1300, 0xe2725b, 0xffa08d)
            }
            "brick_wall" -> {
                drawMode = DrawMode.ZEBRA
                palette = intArrayOf(0x841f27, 0xa94140)
            }
            "chrome" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0xcccccc, 0x999999, 0xbbbbbb, 0xeeeeee, 0xaaaaaa, 0xdddddd)
            }
            "dessert" -> {
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0xa28d68, 0xd5ccbb, 0xa28d68, 0xeae6dd)
            }
            "feeling_blue" -> {
                drawMode = DrawMode.GRADIENT
                palette = intArrayOf(0x000080, 0x000040, 0x000020, 0x000010, 0x000080, 0x000000)
            }
            "forest" -> {
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0x266A2E, 0x4F4F2F, 0x228b22, 0x855E42, 0x347235, 0x8B864E, 0x254117, 0x8B7355)
            }
            "heavy_rain" -> {
                drawMode = DrawMode.ZEBRA_GRADIENT
                palette = intArrayOf(0x000000, 0x0055bb, 0x000000)
            }
            "zebra_stripes" -> {
            }
        }

    }

}
