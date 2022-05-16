package net.perfectdreams.loritta.cinnamon.platform.commands.roblox.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roblox.RobloxUserExecutor

object RobloxCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Minecraft
    val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Roblox

    override fun declaration() = slashCommand(listOf("roblox"), CommandCategory.ROBLOX, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */) {
        subcommand(listOf("user"), I18N_PREFIX.Player.Skin.Description) {
            executor = RobloxUserExecutor
        }
    }
}