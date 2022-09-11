package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.entities.messages.Message
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.OCRCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors

class OCRMessageExecutor(loritta: LorittaCinnamon) : CinnamonMessageCommandExecutor(loritta), OCRExecutor {
    override suspend fun execute(context: ApplicationCommandContext, targetMessage: Message) {
        val image = targetMessage.attachments.firstOrNull {
            it.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES
        } ?: context.failEphemerally {
            styled(
                context.i18nContext.get(OCRCommand.I18N_PREFIX.ThereIsntAnyImageAttachmentOnTheMessage),
                Emotes.LoriHmpf
            )
        }

        context.deferChannelMessageEphemerally()

        handleOCRCommand(
            loritta,
            context,
            true,
            image.url
        )
    }
}