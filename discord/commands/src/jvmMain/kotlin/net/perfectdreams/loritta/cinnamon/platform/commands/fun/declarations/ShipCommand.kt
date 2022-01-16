package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.ShipExecutor

object ShipCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Ship

    override fun declaration() = slashCommand(listOf("ship", "shippar"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = ShipExecutor
    }
}