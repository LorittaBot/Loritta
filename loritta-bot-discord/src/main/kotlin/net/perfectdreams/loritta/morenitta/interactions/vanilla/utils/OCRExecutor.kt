package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.TranslateCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleAPIUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleVisionOCRClient
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleVisionLanguage
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedGoogleVisionOCRResults
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

object OCRExecutor {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Ocr

    suspend fun handleOCRCommand(
        loritta: LorittaBot,
        context: UnleashedContext,
        isEphemeral: Boolean,
        url: String
    ) {
        context.deferChannelMessage(isEphemeral)

        // Is this cached?
        val cachedOcrResult = loritta.transaction {
            CachedGoogleVisionOCRResults.select {
                CachedGoogleVisionOCRResults.url eq url
            }
                .limit(1)
                .firstOrNull()
        }

        val googleVisionResponses = if (cachedOcrResult != null) {
            Json.decodeFromString<GoogleVisionOCRClient.GoogleVisionResponses>(cachedOcrResult[CachedGoogleVisionOCRResults.result])
        } else {
            val responses = loritta.googleVisionOCRClient.ocr(url)

            loritta.transaction {
                CachedGoogleVisionOCRResults.insert {
                    it[CachedGoogleVisionOCRResults.url] = url
                    it[CachedGoogleVisionOCRResults.receivedAt] = Instant.now()
                    it[CachedGoogleVisionOCRResults.result] = Json.encodeToString(responses)
                }
            }

            responses
        }

        val textAnnotations = googleVisionResponses
            .responses
            .firstOrNull()
            ?.textAnnotations
            ?.firstOrNull()
            ?: context.fail(isEphemeral) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.NoTextWasFound),
                    Emotes.LoriSob
                )
            }

        val ocrText = textAnnotations.description
        val detectedOcrLanguageGoogle = textAnnotations.locale?.let { GoogleVisionLanguage.fromLanguageCode(it) }
        val detectedOcrLanguageJDA = GoogleAPIUtils.DISCORD_LOCALE_TO_LANGUAGE_MAP.entries.firstOrNull { it.value == detectedOcrLanguageGoogle }
            ?.key
        val userLocale = context.discordUserLocale

        context.reply(isEphemeral) {
            embed {
                title = "${Emotes.MagnifyingGlassLeft} OCR"

                // Only take 2048 chars
                description = ocrText.take(2048)

                // TODO: Maybe another color?
                color = LorittaColors.LorittaAqua.rgb

                if (detectedOcrLanguageGoogle != null)
                    footer(context.i18nContext.get(I18N_PREFIX.LanguageDetected(context.i18nContext.get(detectedOcrLanguageGoogle.languageNameI18nKey))))
            }

            if (detectedOcrLanguageGoogle != null && detectedOcrLanguageJDA != null && detectedOcrLanguageJDA != userLocale) {
                val userGoogleLocale = GoogleAPIUtils.DISCORD_LOCALE_TO_LANGUAGE_MAP[userLocale]!!

                actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.TranslateToLanguage(context.i18nContext.get(userGoogleLocale.languageNameI18nKey))),
                        {
                            loriEmoji = Emotes.Map
                        }
                    ) {
                        val deferred = it.deferChannelMessage(isEphemeral)

                        val translated = loritta.googleTranslateClient.translate(GoogleAPIUtils.fromVisionLanguageToTranslateLanguage(detectedOcrLanguageGoogle), GoogleAPIUtils.fromVisionLanguageToTranslateLanguage(userGoogleLocale), ocrText)

                        if (translated == null) {
                            deferred.editOriginal {
                                styled(
                                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslationFailed),
                                    Emotes.LoriSob
                                )
                            }
                            return@buttonForUser
                        }

                        deferred.editOriginal {
                            embed {
                                title = "${Emotes.Map} ${
                                    context.i18nContext.get(
                                        I18nKeysData.Commands.Command.Translate.TranslatedFromLanguageToLanguage(
                                            translated.sourceLanguage.languageNameI18nKey,
                                            userGoogleLocale.languageNameI18nKey
                                        )
                                    )
                                }"

                                description = translated.output

                                // TODO: Maybe another color?
                                color = LorittaColors.LorittaAqua.rgb
                            }
                        }
                    }
                )
            }
        }
    }
}