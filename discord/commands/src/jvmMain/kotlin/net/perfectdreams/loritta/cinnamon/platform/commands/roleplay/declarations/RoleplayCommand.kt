package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.*

object RoleplayCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram

    override fun declaration() = slashCommand(listOf("roleplay"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(listOf("hug"), TodoFixThisData) {
            executor = RoleplayHugExecutor
        }

        subcommand(listOf("kiss"), TodoFixThisData) {
            executor = RoleplayKissExecutor
        }

        subcommand(listOf("slap"), TodoFixThisData) {
            executor = RoleplaySlapExecutor
        }

        subcommand(listOf("headpat"), TodoFixThisData) {
            executor = RoleplayHeadPatExecutor
        }

        subcommand(listOf("highfive"), TodoFixThisData) {
            executor = RoleplayHighFiveExecutor
        }

        subcommand(listOf("attack"), TodoFixThisData) {
            executor = RoleplayAttackExecutor
        }

        subcommand(listOf("dance"), TodoFixThisData) {
            executor = RoleplayDanceExecutor
        }
    }
}