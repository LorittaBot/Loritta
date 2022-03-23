package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.social.AddXpExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.RemoveXpExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.SetXpExecutor

object EditXpCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Editxp

    override fun declaration() = slashCommand(listOf("editxp"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        subcommand(listOf("add"), I18N_PREFIX.Add.Description) {
            executor = AddXpExecutor
        }

        subcommand(listOf("remove"), I18N_PREFIX.Remove.Description) {
            executor = RemoveXpExecutor
        }

        subcommand(listOf("set"), I18N_PREFIX.Set.Description) {
            executor = SetXpExecutor
        }
    }
}