package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations

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

class RoleplayCommand(loritta: LorittaCinnamon, val randomRoleplayPicturesClient: RandomRoleplayPicturesClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roleplay
    }

    override fun declaration() = slashCommand("roleplay", CommandCategory.ROLEPLAY, TodoFixThisData) {
        subcommand("hug", I18N_PREFIX.Hug.Description) {
            executor = RoleplayHugExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("kiss", I18N_PREFIX.Kiss.Description) {
            executor = RoleplayKissExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("slap", I18N_PREFIX.Slap.Description) {
            executor = RoleplaySlapExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("headpat", I18N_PREFIX.Headpat.Description) {
            executor = RoleplayHeadPatExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("highfive", I18N_PREFIX.Highfive.Description) {
            executor = RoleplayHighFiveExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("attack", I18N_PREFIX.Attack.Description) {
            executor = RoleplayAttackExecutor(loritta, randomRoleplayPicturesClient)
        }

        subcommand("dance", I18N_PREFIX.Dance.Description) {
            executor = RoleplayDanceExecutor(loritta, randomRoleplayPicturesClient)
        }
    }
}