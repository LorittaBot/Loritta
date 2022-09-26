package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.BanExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.PredefinedReasonsExecutor

class PredefinedReasonsCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    override fun declaration() = slashCommand(I18nKeysData.Commands.Command.Predefinedreasons.Label, CommandCategory.MODERATION, TodoFixThisData) {
        dmPermission = false
        defaultMemberPermissions = Permissions {
            + Permission.BanMembers
        }

        executor = { PredefinedReasonsExecutor(it) }
    }
}