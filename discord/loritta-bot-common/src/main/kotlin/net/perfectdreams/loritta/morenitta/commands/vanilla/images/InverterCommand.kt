package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.awt.Color
import net.perfectdreams.loritta.morenitta.LorittaBot

class InverterCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "invert",
    listOf("inverter"),
    category = net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.invert.description")
    override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

    // TODO: Fix Detailed Usage

    override fun needsToUploadFiles(): Boolean {
        return true
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "invert")

        val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val rgba = image.getRGB(x, y)
                var col = Color(rgba, true)
                col = Color(
                    255 - col.red,
                    255 - col.green,
                    255 - col.blue
                )
                image.setRGB(x, y, col.rgb)
            }
        }

        context.sendFile(image, "invertido.png", context.getAsMention(true))
    }
}