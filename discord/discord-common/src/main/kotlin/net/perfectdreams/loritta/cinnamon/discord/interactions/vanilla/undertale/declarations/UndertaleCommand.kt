package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.CustomTextBoxExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxExecutor

class UndertaleCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Undertale
        val I18N_TEXTBOX_PREFIX = I18nKeysData.Commands.Command.Undertale.Textbox
    }

    override fun declaration() = slashCommand("undertale", CommandCategory.UNDERTALE, TodoFixThisData) {
        subcommandGroup("textbox", I18N_TEXTBOX_PREFIX.Description) {
            subcommand("game", I18N_TEXTBOX_PREFIX.DescriptionGame) {
                executor = { TextBoxExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand("custom", I18N_TEXTBOX_PREFIX.DescriptionCustom) {
                executor = { CustomTextBoxExecutor(it, it.gabrielaImageServerClient) }
            }
        }
    }
}