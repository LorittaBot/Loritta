package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.EmojiInfoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData

class EmojiCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Emoji
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, TodoFixThisData) {
        subcommand(I18N_PREFIX.Info.Label, I18nKeysData.Commands.Command.Emoji.Info.Description) {
            executor = { EmojiInfoExecutor(it) }
        }
    }
}