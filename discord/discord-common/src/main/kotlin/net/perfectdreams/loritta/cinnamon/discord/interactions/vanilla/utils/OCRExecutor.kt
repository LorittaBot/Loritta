package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.OCRCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors

interface OCRExecutor {
    suspend fun handleOCRCommand(
        loritta: LorittaCinnamon,
        context: InteractionContext,
        isEphemeral: Boolean,
        url: String
    ) {
        val textAnnotations = loritta.googleVisionOCRClient.ocr(url)
            .responses
            .firstOrNull()
            ?.textAnnotations
            ?.firstOrNull()

        if (textAnnotations == null) {
            val fail: MessageCreateBuilder.() -> (Unit) = {
                styled(
                    context.i18nContext.get(OCRCommand.I18N_PREFIX.NoTextWasFound),
                    Emotes.LoriSob
                )
            }

            if (isEphemeral)
                context.failEphemerally(fail)
            else
                context.fail(fail)
        }

        val response = textAnnotations.description

        val message: MessageCreateBuilder.() -> (Unit) = {
            embed {
                title = "${Emotes.MagnifyingGlassLeft} OCR"

                description = response

                // TODO: Maybe another color?
                color = LorittaColors.LorittaAqua.toKordColor()
            }
        }

        if (isEphemeral) {
            context.sendEphemeralMessage(message)
        } else {
            context.sendMessage(message)
        }
    }
}