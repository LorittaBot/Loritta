package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.ChooseExecutor

object ChooseCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands
        .Command
        .Choose

    override fun declaration() = slashCommand(listOf("choose", "escolher"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = ChooseExecutor
    }
}