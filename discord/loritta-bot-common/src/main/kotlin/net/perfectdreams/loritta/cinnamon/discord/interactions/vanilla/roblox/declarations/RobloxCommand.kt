package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.RobloxGameExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.RobloxUserExecutor

class RobloxCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roblox
        val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Roblox
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.ROBLOX, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */) {
        subcommand(I18N_PREFIX.User.Label, I18N_PREFIX.User.Description) {
            executor = { RobloxUserExecutor(it, it.http) }
        }

        subcommand(I18N_PREFIX.Game.Label, I18N_PREFIX.Game.Description) {
            executor = { RobloxGameExecutor(it, it.http) }
        }
    }
}