package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse.MorseFromExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.morse.MorseToExecutor

class MorseCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Morse
    }

    override fun declaration() = slashCommand("morse", CommandCategory.UTILS,  I18N_PREFIX.Description) {
        subcommand("to", I18N_PREFIX.DescriptionToMorse) {
            executor = { MorseToExecutor(it) }
        }
        subcommand("from", I18N_PREFIX.DescriptionFromMorse) {
            executor = { MorseFromExecutor(it) }
        }
    }
}