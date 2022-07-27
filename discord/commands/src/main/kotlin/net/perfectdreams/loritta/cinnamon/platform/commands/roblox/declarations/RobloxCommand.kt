package net.perfectdreams.loritta.cinnamon.platform.commands.roblox.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roblox.RobloxGameExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roblox.RobloxUserExecutor

class RobloxCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roblox
        val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Roblox
    }

    override fun declaration() = slashCommand("roblox", CommandCategory.ROBLOX, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */) {
        subcommand("user", I18N_PREFIX.User.Description) {
            executor = { RobloxUserExecutor(it, it.http) }
        }

        subcommand("game", I18N_PREFIX.Game.Description) {
            executor = { RobloxGameExecutor(it, it.http) }
        }
    }
}