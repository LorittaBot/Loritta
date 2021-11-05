package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.HungerGamesExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration

object HungerGamesCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Hungergames

    override fun declaration() = command(listOf("hungergames"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = HungerGamesExecutor
    }
}