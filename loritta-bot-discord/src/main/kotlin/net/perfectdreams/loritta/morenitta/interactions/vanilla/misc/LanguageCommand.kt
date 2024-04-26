package net.perfectdreams.loritta.morenitta.interactions.vanilla.misc

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants

class LanguageCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Language
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MISC) {
        enableLegacyMessageSupport = true
        integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("language")
            add("speak")
            add("lang")
            add("linguagem")
        }

        executor = LanguageCommandExecutor()
    }

    inner class LanguageCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val profile = loritta.getOrCreateLorittaProfile(context.user.idLong)
            val hasPersonalLanguage = loritta.newSuspendedTransaction { profile.settings.language != null && context.guildOrNull == null }

            val validLanguages = listOf(
                LocaleWrapper(
                    loritta.languageManager.getI18nContextByLegacyLocaleId("default"),
                    "\uD83C\uDDE7\uD83C\uDDF7"
                ),
                LocaleWrapper(
                    loritta.languageManager.getI18nContextByLegacyLocaleId("en-us"),
                    "\uD83C\uDDFA\uD83C\uDDF8"
                ),
                LocaleWrapper(
                    loritta.languageManager.getI18nContextByLegacyLocaleId("es"),
                    "\uD83C\uDDEA\uD83C\uDDF8",
                    loritta.localeManager.getLocaleById("es"),
                    "Español"
                )
            )

            context.reply(true) {
                embed {
                    color = Constants.DISCORD_BLURPLE.rgb
                    title = "\uD83C\uDF0E ${context.i18nContext.get(I18N_PREFIX.PleaseSelectYourLanguage)}"

                    description = if (context.guildOrNull == null) {
                        context.i18nContext.get(I18N_PREFIX.ChangeLanguageDescription)
                    } else {
                        context.i18nContext.get(I18N_PREFIX.ChangeServerLanguageDescription)
                    }

                    for (language in validLanguages) {
                        val translators = language.context.get(I18nKeysData.TranslatedBy).mapNotNull { loritta.lorittaShards.retrieveUserInfoById(it.toLong()) }

                        field {
                            name = "${language.emojiName} ${if (language.legacyLocale != null) language.legacyLocaleName else language.context.language.info.name}"
                            value = "**${context.i18nContext.get(I18N_PREFIX.TranslatedBy)}**: ${translators.joinToString(", ") { "`${it.name}`" }}"
                            inline = true
                        }
                    }

                    field {
                        name = context.i18nContext.get(I18N_PREFIX.HelpUsTranslate)
                        value = loritta.config.loritta.crowdin.url
                        inline = false
                    }
                }

                val buttons: List<Button> = buildList {
                    validLanguages.forEach {
                        add(
                            loritta.interactivityManager
                                .buttonForUser(context.user, ButtonStyle.PRIMARY, if (it.legacyLocale != null && it.legacyLocaleName != null) it.legacyLocaleName else it.context.language.info.name, {
                                    emoji = Emoji.fromUnicode(it.emojiName)
                                }) { buttonContext ->
                                    activateLanguage(buttonContext, profile, it, context.guildOrNull == null)
                                }
                        )
                    }

                    add(
                        loritta.interactivityManager
                            .buttonForUser(context.user, ButtonStyle.DANGER, "Redefinir idioma pessoal", {
                                emoji = Emoji.fromUnicode("\uD83D\uDE45")
                            }) {
                                it.deferAndEditOriginal {
                                    actionRow(
                                        loritta.interactivityManager
                                            .disabledButton(ButtonStyle.SUCCESS, context.i18nContext.get(I18N_PREFIX.ButtonPersonalLanguageReset)) {
                                                emoji = Emoji.fromCustom(Emotes.LoriCoffee.name, Emotes.LoriCoffee.id, false)
                                            }
                                    )
                                }

                                loritta.newSuspendedTransaction {
                                    profile.settings.language = null
                                }

                                it.reply(true) {
                                    styled(
                                        it.i18nContext.get(I18N_PREFIX.PersonalLanguageReset)
                                    )
                                }
                            }
                    )
                }

                if (hasPersonalLanguage && context.guildOrNull == null) {
                    actionRow(buttons)
                } else {
                    actionRow(buttons.dropLast(1))
                }
            }
        }

        private suspend fun activateLanguage(context: ComponentContext, profile: Profile, newLanguage: LocaleWrapper, isPrivateChannel: Boolean = false) {
            var localeId = if (newLanguage.legacyLocale != null) {
                newLanguage.legacyLocale.id
            } else {
                when(val localeName = newLanguage.context.language.info.name) {
                    "Português" -> "default"
                    "English" -> "en-us"
                    else -> throw IllegalArgumentException("Unknown locale name: $localeName")
                }
            }

            loritta.newSuspendedTransaction {
                if (isPrivateChannel) {
                    profile.settings.language = localeId
                } else {
                    context.config.localeId = localeId
                }
            }

            val newLocale = loritta.languageManager.getI18nContextByLegacyLocaleId(localeId)

            if (localeId == "default") {
                localeId = "pt-br"
            }

            context.deferAndEditOriginal {
                val emoji = Emotes.LoriCoffee

                actionRow(
                    loritta.interactivityManager.disabledButton(
                        ButtonStyle.SUCCESS,
                        newLocale.get(I18N_PREFIX.ButtonLanguageChanged)
                    ) {
                        this.emoji = Emoji.fromCustom(emoji.name, emoji.id, false)
                    }
                )
            }

            if (isPrivateChannel) {
                context.reply(true) {
                    styled(
                        newLocale.get(I18N_PREFIX.LanguageChanged(localeId))
                    )
                }
            } else {
                val text = StringBuilder().apply {
                    appendLine(
                        LorittaReply(
                            newLocale.get(I18N_PREFIX.ServerLanguageChanged(localeId)),
                        ).build()
                    )
                    appendLine(
                        LorittaReply(
                            newLocale.get(I18N_PREFIX.PersonalLanguageTip),
                        ).build()
                    )
                }

                context.reply(true) {
                    content = text.toString()
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }

    private data class LocaleWrapper(
        val context: I18nContext,
        val emojiName: String,
        val legacyLocale: BaseLocale? = null,
        val legacyLocaleName: String? = null
    )
}