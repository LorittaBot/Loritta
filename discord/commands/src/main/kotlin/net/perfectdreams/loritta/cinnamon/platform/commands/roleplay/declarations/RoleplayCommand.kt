package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayAttackExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayDanceExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHeadPatExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHighFiveExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayHugExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayKissExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplaySlapExecutor
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roleplay
    }

    override fun declaration() = slashCommand("roleplay", CommandCategory.ROLEPLAY, TodoFixThisData) {
        subcommand("hug", I18N_PREFIX.Hug.Description) {
            executor = { RoleplayHugExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("kiss", I18N_PREFIX.Kiss.Description) {
            executor = { RoleplayKissExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("slap", I18N_PREFIX.Slap.Description) {
            executor = { RoleplaySlapExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("headpat", I18N_PREFIX.Headpat.Description) {
            executor = { RoleplayHeadPatExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("highfive", I18N_PREFIX.Highfive.Description) {
            executor = { RoleplayHighFiveExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("attack", I18N_PREFIX.Attack.Description) {
            executor = { RoleplayAttackExecutor(it, it.randomRoleplayPicturesClient) }
        }

        subcommand("dance", I18N_PREFIX.Dance.Description) {
            executor = { RoleplayDanceExecutor(it, it.randomRoleplayPicturesClient) }
        }
    }
}