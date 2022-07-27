package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.EmojiInfoExecutor

class EmojiCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Emoji
    }

    override fun declaration() = slashCommand("emoji", CommandCategory.DISCORD, TodoFixThisData) {
        subcommand("info", I18nKeysData.Commands.Command.Emoji.Info.Description) {
            executor = { EmojiInfoExecutor(it) }
        }
    }
}