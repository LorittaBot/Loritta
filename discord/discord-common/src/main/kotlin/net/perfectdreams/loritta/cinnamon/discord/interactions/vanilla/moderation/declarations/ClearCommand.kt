package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ClearExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager

class ClearCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Clear
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.MODERATION, I18N_PREFIX.Description) {
        dmPermission = false
        defaultMemberPermissions = Permissions {
            + Permission.ManageMessages
        }

        executor = { ClearExecutor(it) }
    }
}