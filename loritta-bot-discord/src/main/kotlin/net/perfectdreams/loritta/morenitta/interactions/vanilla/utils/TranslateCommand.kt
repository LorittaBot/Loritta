package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.interactions.AutoCompleteQuery
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleTranslateLanguage
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class TranslateCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Translate
    }

    override fun command(): SlashCommandDeclarationBuilder = slashCommand(
        name = I18N_PREFIX.Label,
        description = I18N_PREFIX.Description,
        category = CommandCategory.UTILS,
        uniqueId = UUID.fromString("24bb7b9b-096a-4814-ac86-be01c6082855")
    ) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("translate")
        }

        executor = TranslateExecutor()
    }

    inner class TranslateExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val cinnamonAutocomplete: (AutocompleteContext, AutoCompleteQuery, Boolean) -> (Map<String, String>) = { autocompleteContext, focusedCommandOption, includeAuto ->
                val value = focusedCommandOption.value

                GoogleTranslateLanguage.values()
                    .asSequence()
                    .filter {
                        if (!includeAuto) it != GoogleTranslateLanguage.AUTO_DETECT else true
                    }
                    .filter {
                        autocompleteContext.i18nContext.get(it.languageNameI18nKey).startsWith(value, true)
                    }
                    .take(DiscordResourceLimits.Command.Options.ChoicesCount)
                    .associate {
                        autocompleteContext.i18nContext.get(TranslateCommand.I18N_PREFIX.LanguageFormat(it.languageNameI18nKey, it.code)) to it.code
                    }
            }

            val from = string("from", TranslateCommand.I18N_PREFIX.Options.From.Text) {
                autocomplete { autocompleteContext ->
                    cinnamonAutocomplete(autocompleteContext, autocompleteContext.event.focusedOption, true)
                }
            }
            val to = string("to", TranslateCommand.I18N_PREFIX.Options.To.Text) {
                autocomplete { autocompleteContext ->
                    cinnamonAutocomplete(autocompleteContext, autocompleteContext.event.focusedOption, false)
                }
            }

            val text = string("text", TranslateCommand.I18N_PREFIX.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            val from = try {
                GoogleTranslateLanguage.fromLanguageCode(args[options.from])
            } catch (e: NoSuchElementException) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                        Emotes.LoriSob
                    )
                }
                return
            }

            val to = try {
                GoogleTranslateLanguage.fromLanguageCode(args[options.to])
            } catch (e: NoSuchElementException) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                        Emotes.LoriSob
                    )
                }
                return
            }

            if (to == GoogleTranslateLanguage.AUTO_DETECT) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                        Emotes.LoriSob
                    )
                }
                return
            }

            if (from == to) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslatingFromToSameLanguage),
                        Emotes.LoriHmpf
                    )
                }
                return
            }

            val input = args[options.text]

            val translated = loritta.googleTranslateClient.translate(from, to, input)
            if (translated == null) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslationFailed),
                        Emotes.LoriSob
                    )
                }
                return
            }

            context.reply(false) {
                embed {
                    title = "${Emotes.Map} ${
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Translate.TranslatedFromLanguageToLanguage(
                                translated.sourceLanguage.languageNameI18nKey,
                                to.languageNameI18nKey
                            )
                        )
                    }"

                    description = translated.output

                    // TODO: Maybe another color?
                    color = LorittaColors.LorittaAqua.rgb
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (2 > args.size) {
                context.explain()
                return null
            }

            return mapOf(
                options.from to GoogleTranslateLanguage.AUTO_DETECT.code,
                options.to to args[0],
                options.text to args.drop(1).joinToString(" ")
            )
        }
    }
}