package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.CustomTextBoxExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.TextBoxExecutor

class UndertaleCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Undertale
        val I18N_TEXTBOX_PREFIX = I18nKeysData.Commands.Command.Undertale.Textbox
    }

    override fun declaration() = slashCommand("undertale", CommandCategory.UNDERTALE, TodoFixThisData) {
        subcommandGroup("textbox", I18N_TEXTBOX_PREFIX.Description) {
            subcommand("game", I18N_TEXTBOX_PREFIX.DescriptionGame) {
                executor = TextBoxExecutor(loritta, gabiClient)
            }

            subcommand("custom", I18N_TEXTBOX_PREFIX.DescriptionCustom) {
                executor = CustomTextBoxExecutor(loritta, gabiClient)
            }
        }
    }
}