package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.color

import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.addFileData
import net.perfectdreams.loritta.morenitta.utils.ColorUtils
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

class ColorInfoCommandHandler(private val loritta: LorittaBot) {
    companion object {
        private const val FACTOR = 0.7
    }

    suspend fun execute(context: UnleashedContext, color: Color) {
        val hsbVals = Color.RGBtoHSB(color.red, color.green, color.blue, null)

        val hue = hsbVals[0] * 360
        val saturation = hsbVals[1] * 100
        val brightness = hsbVals[2] * 100

        val shadesColors = getShades(color)
        val tintsColors = getTints(color)

        val complementaryColor = Color(Color.HSBtoRGB(((hue + 180) % 360 / 360), saturation / 100, brightness / 100))

        val triadColor1 = Color(Color.HSBtoRGB(((hue + 120) % 360 / 360), saturation / 100, brightness / 100))
        val triadColor2 = Color(Color.HSBtoRGB(((hue - 120) % 360 / 360), saturation / 100, brightness / 100))

        val analogousColor1 = Color(Color.HSBtoRGB(((hue + 30) % 360 / 360), saturation / 100, brightness / 100))
        val analogousColor2 = Color(Color.HSBtoRGB(((hue - 30) % 360 / 360), saturation / 100, brightness / 100))

        val image = generate(
            Color(color.red, color.green, color.blue),
            shadesColors,
            tintsColors,
            Color(triadColor1.red, triadColor1.green, triadColor1.blue),
            Color(triadColor2.red, triadColor2.green, triadColor2.blue),
            Color(analogousColor1.red, analogousColor1.green, analogousColor1.blue),
            Color(analogousColor2.red, analogousColor2.green, analogousColor2.blue),
            Color(complementaryColor.red, complementaryColor.green, complementaryColor.blue),
            context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Shades),
            context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Tints),
            context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Triadic),
            context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Analogous),
            context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Complementary)
        )

        context.reply(ephemeral = false) {
            addFileData("color.png", image.toByteArray(ImageFormatType.PNG))

            embed {
                val colorName = ColorUtils.getColorNameFromColor(color)
                title = "\uD83C\uDFA8 $colorName"

                this.color = color.rgb

                field("RGB", "`${color.red}, ${color.green}, ${color.blue}`", true)
                val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
                field("Hexadecimal", "`$hex`", true)
                field("Decimal", "`${color.rgb}`", true)
                field("HSB", "`${hue.toInt()}°, ${saturation.toInt()}%, ${brightness.toInt()}%`", true)
                field(context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Shades), joinColorsToHex(shadesColors), false)
                field(context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Tints), joinColorsToHex(tintsColors), false)
                field(context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Triadic), joinColorsToHex(listOf(triadColor1, triadColor2)), false)
                field(context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Analogous), joinColorsToHex(listOf(analogousColor1, analogousColor2)), false)
                field(context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Complementary), joinColorsToHex(listOf(complementaryColor)), false)

                this.image = "attachment://color.png"
            }
        }
    }

    private fun generate(
        color: Color,
        shadesColors: List<Color>,
        tintsColors: List<Color>,
        triadColor1: Color,
        triadColor2: Color,
        analogousColor1: Color,
        analogousColor2: Color,
        complementaryColor: Color,
        shades: String,
        tints: String,
        triadic: String,
        analogous: String,
        complementary: String
    ): BufferedImage {
        val colorInfo = BufferedImage(333, 250, BufferedImage.TYPE_INT_ARGB)
        val graphics = colorInfo.graphics

        val font = loritta.graphicsFonts.m5x7.deriveFont(16f)

        graphics.font = font

        // Color Sections
        drawColorSection(0, 0, graphics, shades, shadesColors)

        drawColorSection(0, 61, graphics, tints, tintsColors)

        drawColorSection(0, 122, graphics, triadic, listOf(color, triadColor1, triadColor2))
        drawColorSection(148, 122, graphics, complementary, listOf(color, complementaryColor))

        drawColorSection(0, 183, graphics, analogous, listOf(analogousColor1, analogousColor2))

        // Color Preview (smol circle at the right bottom side of the image)
        val colorPreview = BufferedImage(192, 192, BufferedImage.TYPE_INT_ARGB)
        val previewGraphics = colorPreview.graphics
        previewGraphics.color = color
        previewGraphics.fillRect(0, 0, 192, 192)

        graphics.drawImage(ImageUtils.makeRoundedCorner(colorPreview, 99999), 237, 167, null)

        return colorInfo
    }

    private fun getShades(color: Color): List<Color> {
        val colors = mutableListOf<Color>()

        var shade = Color(color.rgb)
        var previousShade: Int? = null

        while (previousShade != shade.rgb) {
            val newR = shade.red * (1 - FACTOR)
            val newG = shade.green * (1 - FACTOR)
            val newB = shade.blue * (1 - FACTOR)

            previousShade = shade.rgb
            shade = Color(newR.toInt(), newG.toInt(), newB.toInt())
            colors.add(shade)
        }

        return colors
    }

    private fun getTints(color: Color): List<Color> {
        val colors = mutableListOf<Color>()

        var tint = Color(color.rgb)
        var previousTint: Int? = null

        while (previousTint != tint.rgb) {
            val newR = tint.red + (255 - tint.red) * FACTOR
            val newG = tint.green + (255 - tint.green) * FACTOR
            val newB = tint.blue + (255 - tint.blue) * FACTOR

            previousTint = tint.rgb
            tint = Color(newR.toInt(), newG.toInt(), newB.toInt())
            colors.add(tint)
        }

        return colors
    }

    private fun drawColorSection(
        x: Int,
        y: Int,
        graphics: Graphics,
        title: String,
        colors: List<Color>
    ) {
        var currentX = x
        val currentY = y

        fun Graphics.drawWithOutline(text: String, x: Int, y: Int) {
            this.color = Color.BLACK
            this.drawString(text, x - 1, y)
            this.drawString(text, x + 1, y)
            this.drawString(text, x, y - 1)
            this.drawString(text, x, y + 1)
            this.color = Color.WHITE
            this.drawString(text, x, y)
        }

        fun Graphics.drawColor(color: Color, x: Int, y: Int) {
            this.color = color
            this.fillRect(x, y, 48, 48)

            val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)

            var _x = x + 48

            for (char in hex) {
                _x -= this.fontMetrics.charWidth(char)
            }

            this.drawWithOutline(hex, _x - 1, y + 48 - 2)
        }

        graphics.drawWithOutline(title, currentX + 1, currentY + 10)

        for (color in colors) {
            graphics.drawColor(color, currentX, currentY + 13)
            currentX += 48
        }
    }

    fun joinColorsToHex(colors: List<Color>) = colors.joinToString(", ") { "`${String.format("#%02x%02x%02x", it.red, it.green, it.blue)}`" }
}