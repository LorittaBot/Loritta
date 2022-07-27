package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.ChavesCocieloExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.ChavesOpeningExecutor

class ChavesCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Chaves
    }

    override fun declaration() = slashCommand("chaves", CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        subcommand("opening", I18N_PREFIX.Opening.Description) {
            executor = ChavesOpeningExecutor(loritta, gabiClient)
        }

        subcommand("cocielo", I18N_PREFIX.Cocielo.Description) {
            executor = ChavesCocieloExecutor(loritta, gabiClient)
        }
    }
}