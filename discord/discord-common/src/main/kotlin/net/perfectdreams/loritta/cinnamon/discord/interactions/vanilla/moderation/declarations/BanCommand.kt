package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.BanExecutor

class BanCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Ban
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Moderation
    }

    override fun declaration() = slashCommand("ban", CommandCategory.MODERATION, I18N_PREFIX.Description) {
        dmPermission = false
        defaultMemberPermissions = Permissions {
            + Permission.BanMembers
        }

        executor = { BanExecutor(it) }
    }
}