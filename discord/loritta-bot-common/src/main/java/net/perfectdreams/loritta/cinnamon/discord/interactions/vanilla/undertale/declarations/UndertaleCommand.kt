package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.CustomTextBoxExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.TextBoxExecutor

class UndertaleCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Undertale
        val I18N_TEXTBOX_PREFIX = I18nKeysData.Commands.Command.Undertale.Textbox
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.UNDERTALE, TodoFixThisData) {
        subcommandGroup(I18N_TEXTBOX_PREFIX.Label, I18N_TEXTBOX_PREFIX.Description) {
            subcommand(I18N_TEXTBOX_PREFIX.LabelGame, I18N_TEXTBOX_PREFIX.DescriptionGame) {
                executor = { TextBoxExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_TEXTBOX_PREFIX.LabelCustom, I18N_TEXTBOX_PREFIX.DescriptionCustom) {
                executor = { CustomTextBoxExecutor(it, it.gabrielaImageServerClient) }
            }
        }
    }
}