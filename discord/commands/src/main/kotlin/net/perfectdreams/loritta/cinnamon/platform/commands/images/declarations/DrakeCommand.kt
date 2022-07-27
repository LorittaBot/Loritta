package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriDrakeExecutor

class DrakeCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drake
    }

    override fun declaration() = slashCommand("drake", CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand("drake", I18N_PREFIX.Drake.Description) {
            executor = DrakeExecutor(loritta, gabiClient)
        }

        subcommand("bolsonaro", I18N_PREFIX.Bolsonaro.Description) {
            executor = BolsoDrakeExecutor(loritta, gabiClient)
        }

        subcommand("lori", I18N_PREFIX.Lori.Description) {
            executor = LoriDrakeExecutor(loritta, gabiClient)
        }
    }
}