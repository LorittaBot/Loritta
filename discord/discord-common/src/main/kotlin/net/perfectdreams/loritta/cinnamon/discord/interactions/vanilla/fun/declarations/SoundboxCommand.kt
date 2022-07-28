package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox.SoundboardBoardExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox.FalatronExecutor

class SoundboxCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Soundbox
    }

    override fun declaration() = slashCommand("soundbox", CommandCategory.FUN, I18N_PREFIX.Description) {
        defaultMemberPermissions = Permissions {
            + Permission.MoveMembers
        }
        dmPermission = false

        subcommand("falatron", I18N_PREFIX.Falatron.Description) {
            executor = { FalatronExecutor(it, it.falatronModelsManager) }
        }

        subcommand("board", I18N_PREFIX.Board.Description) {
            executor = { SoundboardBoardExecutor(it) }
        }
    }
}