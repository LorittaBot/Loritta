package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions

class OCRSlashExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta), OCRExecutor {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val imageReference = imageReferenceOrAttachment("image")
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val image = args[options.imageReference].get(context)!! // Shouldn't be null here because it is required

        context.deferChannelMessage()

        handleOCRCommand(
            loritta,
            context,
            false,
            image.url
        )
    }
}