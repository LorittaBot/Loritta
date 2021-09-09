package net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.commands.`fun`.ShipExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object ShipCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Ship

    override fun declaration() = command(listOf("ship", "shippar"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = ShipExecutor
    }
}