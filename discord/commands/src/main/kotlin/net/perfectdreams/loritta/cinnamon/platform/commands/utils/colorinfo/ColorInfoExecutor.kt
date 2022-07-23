package net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo

import dev.kord.common.kColor
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.ColorInfoRequest
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.ColorInfoCommand
import java.awt.Color

abstract class ColorInfoExecutor(val client: GabrielaImageServerClient) : SlashCommandExecutor() {
    suspend fun executeWithColor(context: ApplicationCommandContext, color: Color) {
        context.deferChannelMessage()

        val hsbVals = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsbVals)

        val hue = hsbVals[0] * 360
        val saturation = hsbVals[1] * 100
        val brightness = hsbVals[2] * 100

        val complementaryColor = Color(Color.HSBtoRGB(((hue + 180) % 360 / 360), saturation / 100, brightness / 100))

        val triadColor1 = Color(Color.HSBtoRGB(((hue + 120) % 360 / 360), saturation / 100, brightness / 100))
        val triadColor2 = Color(Color.HSBtoRGB(((hue - 120) % 360 / 360), saturation / 100, brightness / 100))

        val analogousColor1 = Color(Color.HSBtoRGB(((hue + 30) % 360 / 360), saturation / 100, brightness / 100))
        val analogousColor2 = Color(Color.HSBtoRGB(((hue - 30) % 360 / 360), saturation / 100, brightness / 100))

        val image = client.images.colorInfo(
            ColorInfoRequest(
                ColorInfoRequest.Color(color.red, color.green, color.blue),
                ColorInfoRequest.Color(triadColor1.red, triadColor1.green, triadColor1.blue),
                ColorInfoRequest.Color(triadColor2.red, triadColor2.green, triadColor2.blue),
                ColorInfoRequest.Color(analogousColor1.red, analogousColor1.green, analogousColor1.blue),
                ColorInfoRequest.Color(analogousColor2.red, analogousColor2.green, analogousColor2.blue),
                ColorInfoRequest.Color(complementaryColor.red, complementaryColor.green, complementaryColor.blue),
                context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Shades),
                context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Tints),
                context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Triadic),
                context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Analogous),
                context.i18nContext.get(ColorInfoCommand.I18N_PREFIX.Complementary)
            )
        )

        context.sendMessage {
            addFile("color.png", image.inputStream())

            embed {
                val colorName = ColorUtils.getColorNameFromColor(color)
                title = "\uD83C\uDFA8 $colorName"

                this.color = color.kColor

                field("RGB", "`${color.red}, ${color.green}, ${color.blue}`", true)
                val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
                field("Hexadecimal", "`$hex`", true)
                field("Decimal", "`${color.rgb}`", true)
                field("HSB", "`${hue.toInt()}°, ${saturation.toInt()}%, ${brightness.toInt()}%`", true)

                this.image = "attachment://color.png"
            }
        }
    }
}