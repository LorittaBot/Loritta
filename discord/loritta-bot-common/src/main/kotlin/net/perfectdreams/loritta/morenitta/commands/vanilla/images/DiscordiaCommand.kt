package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.gifs.MentionGIF
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class DiscordiaCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "mentions",
    listOf("discórdia", "discord", "discordia"),
    net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.discordping.description")
    override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

    // TODO: Fix Usage

    override fun needsToUploadFiles() = true

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "discordping")

        var contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
        var file = MentionGIF.getGIF(contextImage)
        loritta.gifsicle.optimizeGIF(file)
        context.sendFile(file, "discordia.gif", context.getAsMention(true))
        file.delete()
    }
}