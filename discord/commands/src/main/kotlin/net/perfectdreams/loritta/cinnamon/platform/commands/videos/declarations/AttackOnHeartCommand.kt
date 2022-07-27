package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.AttackOnHeartExecutor

class AttackOnHeartCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Attackonheart
    }

    override fun declaration() = slashCommand("attackonheart", CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = { AttackOnHeartExecutor(it, it.gabrielaImageServerClient) }
    }
}