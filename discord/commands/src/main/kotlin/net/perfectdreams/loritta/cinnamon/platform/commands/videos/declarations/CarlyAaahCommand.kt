package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.CarlyAaahExecutor

class CarlyAaahCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Carlyaaah
    }

    override fun declaration() = slashCommand("carlyaaah", CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = CarlyAaahExecutor(loritta, gabiClient)
    }
}