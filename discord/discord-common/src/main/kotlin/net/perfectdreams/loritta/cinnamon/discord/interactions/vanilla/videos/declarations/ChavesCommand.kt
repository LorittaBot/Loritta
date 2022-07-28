package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.ChavesCocieloExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.ChavesOpeningExecutor

class ChavesCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Chaves
    }

    override fun declaration() = slashCommand("chaves", CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        subcommand("opening", I18N_PREFIX.Opening.Description) {
            executor = { ChavesOpeningExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand("cocielo", I18N_PREFIX.Cocielo.Description) {
            executor = { ChavesCocieloExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}