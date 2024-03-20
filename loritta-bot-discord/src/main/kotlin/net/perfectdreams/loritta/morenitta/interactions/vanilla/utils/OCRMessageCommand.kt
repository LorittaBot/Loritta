package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.messageCommand

class OCRMessageCommand(val loritta: LorittaBot) : MessageCommandDeclarationWrapper {
    override fun command() = messageCommand(OCRExecutor.I18N_PREFIX.ReadTextFromImage, CommandCategory.UTILS, OCRMessageExecutor()) {
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)
    }

    inner class OCRMessageExecutor : LorittaMessageCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val attachment = message.attachments.firstOrNull()
            val url = attachment?.url
            if (url == null || attachment.contentType == null || attachment.contentType !in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES)
                context.fail(true) {
                    styled(
                        context.i18nContext.get(OCRExecutor.I18N_PREFIX.ThereIsntAnyImageAttachmentOnTheMessage),
                        Emotes.LoriHmpf
                    )
                }

            OCRExecutor.handleOCRCommand(
                loritta,
                context,
                true,
                url
            )
        }
    }
}