package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.commands.images.DrakeExecutor
import net.perfectdreams.loritta.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object DrakeCommand : CommandDeclaration {
    override fun declaration() = command(listOf("drake"), CommandCategory.IMAGES, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("drake"), LocaleKeyData("commands.command.drake.description").toI18nHelper()) {
            executor = DrakeExecutor
        }

        subcommand(listOf("bolsonaro"), LocaleKeyData("commands.command.bolsodrake.description").toI18nHelper()) {
            executor = BolsoDrakeExecutor
        }

        subcommand(listOf("lori"), LocaleKeyData("commands.command.loridrake.description").toI18nHelper()) {
            executor = LoriDrakeExecutor
        }
    }
}