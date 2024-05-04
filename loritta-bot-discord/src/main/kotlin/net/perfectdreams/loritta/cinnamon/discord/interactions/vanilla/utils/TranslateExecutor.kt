package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.TranslateCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleTranslateLanguage
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot

class TranslateExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    val cinnamonAutocomplete: (AutocompleteContext, FocusedCommandOption, Boolean) -> (Map<String, String>) = { autocompleteContext, focusedCommandOption, includeAuto ->
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

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val from = string("from", TranslateCommand.I18N_PREFIX.Options.From.Text) {
            cinnamonAutocomplete { autocompleteContext, focusedCommandOption ->
                cinnamonAutocomplete.invoke(autocompleteContext, focusedCommandOption, true)
            }
        }
        val to = string("to", TranslateCommand.I18N_PREFIX.Options.To.Text) {
            cinnamonAutocomplete { autocompleteContext, focusedCommandOption ->
                cinnamonAutocomplete.invoke(autocompleteContext, focusedCommandOption, false)
            }
        }
        val text = string("text", TranslateCommand.I18N_PREFIX.Options.Text.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val from = try {
            GoogleTranslateLanguage.fromLanguageCode(args[options.from])
        } catch (e: NoSuchElementException) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                    Emotes.LoriSob
                )
            }
        }

        val to = try {
            GoogleTranslateLanguage.fromLanguageCode(args[options.to])
        } catch (e: NoSuchElementException) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                    Emotes.LoriSob
                )
            }
        }

        if (to == GoogleTranslateLanguage.AUTO_DETECT) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.InvalidLanguage),
                    Emotes.LoriSob
                )
            }
        }

        if (from == to) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslatingFromToSameLanguage),
                    Emotes.LoriHmpf
                )
            }
        }

        val input = args[options.text]

        val translated = loritta.googleTranslateClient.translate(from, to, input)
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(TranslateCommand.I18N_PREFIX.TranslationFailed),
                    Emotes.LoriSob
                )
            }

        context.sendMessage {
            embed {
                title = "${Emotes.Map} ${context.i18nContext.get(I18nKeysData.Commands.Command.Translate.TranslatedFromLanguageToLanguage(translated.sourceLanguage.languageNameI18nKey, to.languageNameI18nKey))}"

                description = translated.output

                // TODO: Maybe another color?
                color = LorittaColors.LorittaAqua.toKordColor()
            }
        }
    }
}