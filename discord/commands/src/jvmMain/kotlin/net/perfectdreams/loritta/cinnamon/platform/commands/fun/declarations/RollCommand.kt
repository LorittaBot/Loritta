package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object RollCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Roll

    override fun declaration() = command(listOf("roll"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = RollExecutor
    }
}