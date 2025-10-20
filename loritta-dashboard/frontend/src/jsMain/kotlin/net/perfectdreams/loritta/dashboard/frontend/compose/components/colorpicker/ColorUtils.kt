package net.perfectdreams.loritta.dashboard.frontend.compose.components.colorpicker

import kotlin.math.floor

object ColorUtils {
    val LorittaAqua = Color(41, 166, 254)

    val defaultColorsCombo = listOf(
        ColorPair(
            Color(26, 188, 156),
            Color(17, 128, 106)
        ),

        ColorPair(
            Color(46, 204, 113),
            Color(31, 139, 76)
        ),

        ColorPair(
            Color(52, 152, 219),
            Color(32, 102, 148)
        ),

        // ===[ ADDITIONAL COLOR (LORITTA) ]===
        ColorPair(
            LorittaAqua,
            Color(30, 123, 189)
        ),

        // ===[ ADDITIONAL COLOR (DISCORD BRANDING) ]===
        ColorPair(
            Color(88, 101, 242),
            Color(31, 44, 191)
        ),

        ColorPair(
            Color(155, 89, 182),
            Color(113, 54, 138)
        ),

        ColorPair(
            Color(233, 30, 99),
            Color(173, 20, 87)
        ),

        ColorPair(
            Color(241, 196, 15),
            Color(194, 124, 14)
        ),

        ColorPair(
            Color(230, 126, 34),
            Color(168, 67, 0)
        ),

        ColorPair(
            Color(231, 76, 60),
            Color(153, 45, 34)
        ),

        ColorPair(
            Color(149, 165, 166),
            Color(151, 156, 159)
        ),

        ColorPair(
            Color(96, 125, 139),
            Color(84, 110, 122)
        ),
    )

    val defaultColors = defaultColorsCombo.flatMap { listOf(it.color1, it.color2) }

    // Thanks, ChatGPT!
    val pastelColors = listOf(
        Color(255, 223, 186),  // Pastel Peach
        Color(173, 216, 230),  // Pastel Blue
        Color(221, 160, 221),  // Pastel Purple
        Color(152, 251, 152),  // Pastel Green
        Color(255, 182, 193),  // Pastel Pink
        Color(255, 228, 196),  // Pastel Orange
        Color(240, 128, 128),  // Pastel Red
        Color(255, 248, 220),  // Pastel Yellow
        Color(220, 220, 220),  // Pastel Gray
        Color(255, 250, 250),  // Pastel Ivory
        Color(245, 222, 179),  // Pastel Beige
        Color(188, 143, 143),  // Pastel Brown
        Color(152, 251, 152),  // Pastel Mint
        Color(255, 192, 203),  // Pastel Salmon
        Color(176, 224, 230)   // Pastel Turquoise
    )

    fun convertFromColorToHex(input: Int): String {
        val red = input shr 16 and 0xFF
        val green = input shr 8 and 0xFF
        val blue = input and 0xFF

        val hexRed = red.toString(16).padStart(2, '0')
        val hexGreen = green.toString(16).padStart(2, '0')
        val hexBlue = blue.toString(16).padStart(2, '0')
        return "#$hexRed$hexGreen$hexBlue"
    }

    fun convertFromHexToColor(hex: String) = Color.fromHex(hex)

    // From ChatGPT xd
    fun calculateBrightness(color: Color) = 0.299 * (color.red / 255.0) + 0.587 * (color.green / 255.0) + 0.114 * (color.blue / 255.0)

    // From ChatGPT xd
    fun HSBtoHSL(hue: Float, saturation: Float, brightness: Float): Triple<Float, Float, Float> {
        val l = (2 - saturation) * brightness / 2

        val sl = when {
            l < 0.5f -> saturation * brightness / (l * 2)
            else -> saturation * brightness / (2 - l * 2)
        }

        val hslHue = when (hue) {
            in 0.0f..1.0f -> hue
            else -> hue % 1
        }

        return Triple(hslHue, sl, l)
    }

    /**
     * Converts the components of a color, as specified by the default RGB
     * model, to an equivalent set of values for hue, saturation, and
     * brightness that are the three components of the HSB model.
     *
     *
     * If the `hsbvals` argument is `null`, then a
     * new array is allocated to return the result. Otherwise, the method
     * returns the array `hsbvals`, with the values put into
     * that array.
     * @param     r   the red component of the color
     * @param     g   the green component of the color
     * @param     b   the blue component of the color
     * @param     hsbvals  the array used to return the
     * three HSB values, or `null`
     * @return    an array of three elements containing the hue, saturation,
     * and brightness (in that order), of the color with
     * the indicated red, green, and blue components.
     * @see java.awt.Color.getRGB
     * @see java.awt.Color.Color
     * @see java.awt.image.ColorModel.getRGBdefault
     * @since     1.0
     */
    // From JDK
    fun RGBtoHSB(r: Int, g: Int, b: Int, hsbvals: FloatArray?): FloatArray {
        var hsbvals = hsbvals
        var hue: Float
        val saturation: Float
        val brightness: Float
        if (hsbvals == null) {
            hsbvals = FloatArray(3)
        }
        var cmax = if (r > g) r else g
        if (b > cmax) cmax = b
        var cmin = if (r < g) r else g
        if (b < cmin) cmin = b
        brightness = cmax.toFloat() / 255.0f
        saturation = if (cmax != 0) (cmax - cmin).toFloat() / cmax.toFloat() else 0f
        if (saturation == 0f) hue = 0f else {
            val redc = (cmax - r).toFloat() / (cmax - cmin).toFloat()
            val greenc = (cmax - g).toFloat() / (cmax - cmin).toFloat()
            val bluec = (cmax - b).toFloat() / (cmax - cmin).toFloat()
            hue = if (r == cmax) bluec - greenc else if (g == cmax) 2.0f + redc - bluec else 4.0f + greenc - redc
            hue /= 6.0f
            if (hue < 0) hue += 1.0f
        }
        hsbvals[0] = hue
        hsbvals[1] = saturation
        hsbvals[2] = brightness
        return hsbvals
    }

    /**
     * Converts the components of a color, as specified by the HSB
     * model, to an equivalent set of values for the default RGB model.
     *
     *
     * The `saturation` and `brightness` components
     * should be floating-point values between zero and one
     * (numbers in the range 0.0-1.0).  The `hue` component
     * can be any floating-point number.  The floor of this number is
     * subtracted from it to create a fraction between 0 and 1.  This
     * fractional number is then multiplied by 360 to produce the hue
     * angle in the HSB color model.
     *
     *
     * The integer that is returned by `HSBtoRGB` encodes the
     * value of a color in bits 0-23 of an integer value that is the same
     * format used by the method [getRGB][.getRGB].
     * This integer can be supplied as an argument to the
     * `Color` constructor that takes a single integer argument.
     * @param     hue   the hue component of the color
     * @param     saturation   the saturation of the color
     * @param     brightness   the brightness of the color
     * @return    the RGB value of the color with the indicated hue,
     * saturation, and brightness.
     * @see java.awt.Color.getRGB
     * @see java.awt.Color.Color
     * @see java.awt.image.ColorModel.getRGBdefault
     * @since     1.0
     */
// From JDK
    fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
        var r = 0
        var g = 0
        var b = 0
        if (saturation == 0f) {
            b = (brightness * 255.0f + 0.5f).toInt()
            g = b
            r = g
        } else {
            val h: Float = (hue - floor(hue.toDouble()).toFloat()) * 6.0f
            val f: Float = h - floor(h.toDouble()).toFloat()
            val p = brightness * (1.0f - saturation)
            val q = brightness * (1.0f - saturation * f)
            val t = brightness * (1.0f - saturation * (1.0f - f))
            when (h.toInt()) {
                0 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (t * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }

                1 -> {
                    r = (q * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }

                2 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (t * 255.0f + 0.5f).toInt()
                }

                3 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (q * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }

                4 -> {
                    r = (t * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }

                5 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (q * 255.0f + 0.5f).toInt()
                }
            }
        }
        return -0x1000000 or (r shl 16) or (g shl 8) or (b shl 0)
    }

    data class ColorPair(val color1: Color, val color2: Color)
}