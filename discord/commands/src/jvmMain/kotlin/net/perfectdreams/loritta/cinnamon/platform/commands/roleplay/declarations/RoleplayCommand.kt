package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHeadPatExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHighFiveExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHugExecutor

object RoleplayCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram

    override fun declaration() = slashCommand(listOf("roleplay"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(listOf("hug"), TodoFixThisData) {
            executor = RoleplayHugExecutor
        }

        subcommand(listOf("headpat"), TodoFixThisData) {
            executor = RoleplayHeadPatExecutor
        }

        subcommand(listOf("highfive"), TodoFixThisData) {
            executor = RoleplayHighFiveExecutor
        }
    }
}