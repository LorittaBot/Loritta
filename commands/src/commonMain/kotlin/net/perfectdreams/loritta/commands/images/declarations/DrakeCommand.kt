package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.commands.images.DrakeExecutor
import net.perfectdreams.loritta.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object DrakeCommand : CommandDeclaration {
    override fun declaration() = command(listOf("drake"), CommandCategory.IMAGES) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("drake")) {
            description = LocaleKeyData("commands.command.drake.description")
            executor = DrakeExecutor
        }

        subcommand(listOf("bolsonaro")) {
            description = LocaleKeyData("commands.command.bolsodrake.description")
            executor = BolsoDrakeExecutor
        }

        subcommand(listOf("lori")) {
            description = LocaleKeyData("commands.command.loridrake.description")
            executor = LoriDrakeExecutor
        }
    }
}