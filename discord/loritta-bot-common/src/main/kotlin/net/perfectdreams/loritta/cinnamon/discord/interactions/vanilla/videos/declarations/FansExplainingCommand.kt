package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.FansExplainingExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

class FansExplainingCommand(languageManager: LanguageManager) :
    CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Fansexplaining
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = { FansExplainingExecutor(it, it.gabrielaImageServerClient) }
    }
}