package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.soundbox.FalatronExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

class SoundboxCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Soundbox
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.FUN, I18N_PREFIX.Description) {
        defaultMemberPermissions = Permissions {
            + Permission.MoveMembers
        }
        dmPermission = false

        subcommand(I18N_PREFIX.Falatron.Label, I18N_PREFIX.Falatron.Description) {
            executor = { FalatronExecutor(it, it.falatronModelsManager) }
        }
    }
}