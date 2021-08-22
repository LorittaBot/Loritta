package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.commands.images.DrakeExecutor
import net.perfectdreams.loritta.commands.images.LoriDrakeExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object DrakeCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Drake

    override fun declaration() = command(listOf("drake"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand(listOf("drake"), I18N_PREFIX.Drake.Description) {
            executor = DrakeExecutor
        }

        subcommand(listOf("bolsonaro"), I18N_PREFIX.Bolsonaro.Description) {
            executor = BolsoDrakeExecutor
        }

        subcommand(listOf("lori"), I18N_PREFIX.Lori.Description) {
            executor = LoriDrakeExecutor
        }
    }
}