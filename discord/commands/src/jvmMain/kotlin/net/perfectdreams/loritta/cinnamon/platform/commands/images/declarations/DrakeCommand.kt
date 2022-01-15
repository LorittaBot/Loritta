package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriDrakeExecutor

object DrakeCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Drake

    override fun declaration() = slashCommand(listOf("drake"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
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