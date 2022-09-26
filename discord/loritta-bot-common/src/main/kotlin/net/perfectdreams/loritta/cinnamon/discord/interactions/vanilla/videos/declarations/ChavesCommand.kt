package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.ChavesCocieloExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.ChavesOpeningExecutor

class ChavesCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Chaves
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.Opening.Label, I18N_PREFIX.Opening.Description) {
            executor = { ChavesOpeningExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Cocielo.Label, I18N_PREFIX.Cocielo.Description) {
            executor = { ChavesCocieloExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}