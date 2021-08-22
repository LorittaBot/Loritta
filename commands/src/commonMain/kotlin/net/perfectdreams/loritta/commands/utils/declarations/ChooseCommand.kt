package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object ChooseCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands
        .Command
        .Choose

    override fun declaration() = command(listOf("choose", "escolher"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = ChooseExecutor
    }
}