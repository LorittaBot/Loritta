package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr

import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.embed
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.TranslateCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors

class OCRTranslateButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.OCR_TRANSLATE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<OCRTranslateData>()

        val translated = loritta.googleTranslateClient.translate(data.sourceLanguage, data.targetLanguage, data.text)
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslationFailed),
                    Emotes.LoriSob
                )
            }

        val message: suspend MessageCreateBuilder.() -> (Unit) = {
            embed {
                title = "${Emotes.Map} ${
                    context.i18nContext.get(
                        I18nKeysData.Commands.Command.Translate.TranslatedFromLanguageToLanguage(
                            translated.sourceLanguage.languageNameI18nKey,
                            data.targetLanguage.languageNameI18nKey
                        )
                    )
                }"

                description = translated.output

                // TODO: Maybe another color?
                color = LorittaColors.LorittaAqua.toKordColor()
            }
        }

        if (data.isEphemeral) {
            context.sendEphemeralMessage {
                message()
            }
        } else {
            context.sendMessage {
                message()
            }
        }
    }
}