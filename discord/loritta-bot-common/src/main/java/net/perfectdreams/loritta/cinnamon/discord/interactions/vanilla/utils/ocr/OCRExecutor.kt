package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithHybridData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.OCRCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.google.Language
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors

interface OCRExecutor {
    companion object {
        private val LOCALE_TO_LANGUAGE_MAP = Locale.ALL.map {
            it to when (it) {
                Locale.PORTUGUESE_BRAZIL -> Language.PORTUGUESE
                Locale.BULGARIAN -> Language.BULGARIAN
                Locale.CHINESE_CHINA -> Language.SIMPLIFIED_CHINESE
                Locale.CHINESE_TAIWAN -> Language.SIMPLIFIED_CHINESE
                Locale.CROATIAN -> Language.CROATIAN
                Locale.CZECH -> Language.CZECH
                Locale.DANISH -> Language.DANISH
                Locale.DUTCH -> Language.DUTCH
                Locale.FINNISH -> Language.FINNISH
                Locale.FRENCH -> Language.FRENCH
                Locale.GERMAN -> Language.GERMAN
                Locale.GREEK -> Language.GREEK
                Locale.HINDI -> Language.HINDI
                Locale.HUNGARIAN -> Language.HUNGARIAN
                Locale.ITALIAN -> Language.ITALIAN
                Locale.JAPANESE -> Language.JAPANESE
                Locale.KOREAN -> Language.KOREAN
                Locale.LITHUANIAN -> Language.LITHUANIAN
                Locale.NORWEGIAN -> Language.NORWEGIAN
                Locale.POLISH -> Language.POLISH
                Locale.ROMANIAN -> Language.ROMANIAN
                Locale.RUSSIAN -> Language.RUSSIAN
                Locale.SPANISH_SPAIN -> Language.SPANISH
                Locale.SWEDISH -> Language.SWEDISH
                Locale.THAI -> Language.THAI
                Locale.TURKISH -> Language.TURKISH
                Locale.UKRAINIAN -> Language.UKRAINIAN
                Locale.VIETNAMESE -> Language.VIETNAMESE
                Locale.ENGLISH_GREAT_BRITAIN -> Language.ENGLISH
                else -> Language.ENGLISH
            }
        }.toMap()
    }

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

        val ocrText = textAnnotations.description
        val detectedOcrLanguageGoogle = textAnnotations.locale?.let { Language.fromLanguageCode(it) }
        val detectedOcrLanguageKord = LOCALE_TO_LANGUAGE_MAP.entries.firstOrNull { it.value == detectedOcrLanguageGoogle }
            ?.key
        val userLocale = context.interaKTionsContext.discordInteraction.locale.value ?: context.interaKTionsContext.discordInteraction.guildLocale.value

        val message: suspend MessageCreateBuilder.() -> (Unit) = {
            embed {
                title = "${Emotes.MagnifyingGlassLeft} OCR"

                description = ocrText

                // TODO: Maybe another color?
                color = LorittaColors.LorittaAqua.toKordColor()

                if (detectedOcrLanguageGoogle != null)
                    footer(context.i18nContext.get(OCRCommand.I18N_PREFIX.LanguageDetected(context.i18nContext.get(detectedOcrLanguageGoogle.languageNameI18nKey))))
            }

            if (detectedOcrLanguageGoogle != null && userLocale != null && detectedOcrLanguageKord != null && detectedOcrLanguageKord != userLocale) {
                val userGoogleLocale = LOCALE_TO_LANGUAGE_MAP[userLocale]!!

                actionRow {
                    interactiveButtonWithHybridData(
                        loritta,
                        ButtonStyle.Primary,
                        OCRTranslateButtonExecutor,
                        OCRTranslateData(
                            context.user.id,
                            detectedOcrLanguageGoogle,
                            userGoogleLocale,
                            isEphemeral,
                            ocrText
                        )
                    ) {
                        loriEmoji = Emotes.Map
                        label = context.i18nContext.get(OCRCommand.I18N_PREFIX.TranslateToLanguage(context.i18nContext.get(userGoogleLocale.languageNameI18nKey)))
                    }
                }
            }
        }

        if (isEphemeral) {
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