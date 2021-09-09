package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object ChooseCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands
        .Command
        .Choose

    override fun declaration() = command(listOf("choose", "escolher"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = ChooseExecutor
    }
}