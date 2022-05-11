package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayAttackExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayDanceExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHeadPatExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHighFiveExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHugExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayKissExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplaySlapExecutor

object RoleplayCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Roleplay

    override fun declaration() = slashCommand(listOf("roleplay"), CommandCategory.ROLEPLAY, TodoFixThisData) {
        subcommand(listOf("hug"), I18N_PREFIX.Hug.Description) {
            executor = RoleplayHugExecutor
        }

        subcommand(listOf("kiss"), I18N_PREFIX.Kiss.Description) {
            executor = RoleplayKissExecutor
        }

        subcommand(listOf("slap"), I18N_PREFIX.Slap.Description) {
            executor = RoleplaySlapExecutor
        }

        subcommand(listOf("headpat"), I18N_PREFIX.Headpat.Description) {
            executor = RoleplayHeadPatExecutor
        }

        subcommand(listOf("highfive"), I18N_PREFIX.Highfive.Description) {
            executor = RoleplayHighFiveExecutor
        }

        subcommand(listOf("attack"), I18N_PREFIX.Attack.Description) {
            executor = RoleplayAttackExecutor
        }

        subcommand(listOf("dance"), I18N_PREFIX.Dance.Description) {
            executor = RoleplayDanceExecutor
        }
    }
}