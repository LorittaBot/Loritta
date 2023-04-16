package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Roleplay
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.ROLEPLAY) {
        subcommand(I18N_PREFIX.Hug.Label, I18N_PREFIX.Hug.Description) {
            executor = RoleplayHugExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Kiss.Label, I18N_PREFIX.Kiss.Description) {
            executor = RoleplayKissExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Slap.Label, I18N_PREFIX.Slap.Description) {
            executor = RoleplaySlapExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Headpat.Label, I18N_PREFIX.Headpat.Description) {
            executor = RoleplayHeadPatExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Highfive.Label, I18N_PREFIX.Highfive.Description) {
            executor = RoleplayHighFiveExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Attack.Label, I18N_PREFIX.Attack.Description) {
            executor = RoleplayAttackExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }

        subcommand(I18N_PREFIX.Dance.Label, I18N_PREFIX.Dance.Description) {
            executor = RoleplayDanceExecutor(loritta, loritta.randomRoleplayPicturesClient)
        }
    }

    class RoleplayHugExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.HUG_ATTRIBUTES
    )

    class RoleplayKissExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.KISS_ATTRIBUTES
    )

    class RoleplaySlapExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.SLAP_ATTRIBUTES
    )

    class RoleplayHeadPatExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.HEAD_PAT_ATTRIBUTES
    )

    class RoleplayHighFiveExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.HIGH_FIVE_ATTRIBUTES
    )

    class RoleplayAttackExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.ATTACK_ATTRIBUTES
    )

    class RoleplayDanceExecutor(
        loritta: LorittaBot,
        client: RandomRoleplayPicturesClient,
    ) : RoleplayPictureExecutor(
        loritta,
        client,
        RoleplayUtils.DANCE_ATTRIBUTES
    )
}