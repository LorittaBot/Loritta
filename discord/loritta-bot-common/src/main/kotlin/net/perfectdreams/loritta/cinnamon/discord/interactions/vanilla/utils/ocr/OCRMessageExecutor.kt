package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr

import net.perfectdreams.discordinteraktions.common.entities.messages.Message
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonMessageCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.OCRCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class OCRMessageExecutor(loritta: LorittaBot) : CinnamonMessageCommandExecutor(loritta), OCRExecutor {
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