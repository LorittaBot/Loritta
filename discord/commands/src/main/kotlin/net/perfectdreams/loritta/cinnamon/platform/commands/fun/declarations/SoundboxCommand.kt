package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.SoundboardBoardExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.FalatronExecutor

object SoundboxCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(listOf("soundbox"), CommandCategory.FUN, TodoFixThisData) {
        defaultMemberPermissions = Permissions {
            + Permission.MoveMembers
        }
        dmPermission = false

        subcommand(listOf("falatron"), TodoFixThisData) {
            executor = FalatronExecutor
        }

        subcommand(listOf("board"), TodoFixThisData) {
            executor = SoundboardBoardExecutor
        }
    }
}