package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.SoundboardBoardExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox.FalatronExecutor

object SoundboxCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Soundbox

    override fun declaration() = slashCommand(listOf("soundbox"), CommandCategory.FUN, I18N_PREFIX.Description) {
        defaultMemberPermissions = Permissions {
            + Permission.MoveMembers
        }
        dmPermission = false

        subcommand(listOf("falatron"), I18N_PREFIX.Falatron.Description) {
            executor = FalatronExecutor
        }

        subcommand(listOf("board"), I18N_PREFIX.Board.Description) {
            executor = SoundboardBoardExecutor
        }
    }
}