package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.CustomTextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxExecutor

object UndertaleCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Undertale
    val I18N_TEXTBOX_PREFIX = I18nKeysData.Commands.Command.Undertale.Textbox

    override fun declaration() = slashCommand(listOf("undertale"), CommandCategory.UNDERTALE, TodoFixThisData) {
        subcommandGroup(listOf("textbox"), I18N_TEXTBOX_PREFIX.Description) {
            subcommand(listOf("game"), I18N_TEXTBOX_PREFIX.DescriptionGame) {
                executor = TextBoxExecutor
            }

            subcommand(listOf("custom"), I18N_TEXTBOX_PREFIX.DescriptionCustom) {
                executor = CustomTextBoxExecutor
            }
        }
    }
}