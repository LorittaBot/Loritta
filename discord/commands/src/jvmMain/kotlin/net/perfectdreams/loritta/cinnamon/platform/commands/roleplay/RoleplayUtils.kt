package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.*
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source.SourcePictureExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureSource

object RoleplayUtils {
    val HUG_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::hug,
        RetributeHugButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Hug.Response(user1Mention, user2Mention) },
        Color(255, 141, 230),
        Emotes.Blush
    )

    val HEAD_PAT_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::headPat,
        RetributeHeadPatButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Headpat.Response(user1Mention, user2Mention) },
        Color(156, 39, 176),
        Emotes.LoriPat
    )

    val HIGH_FIVE_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::highFive,
        RetributeHighFiveButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Highfive.Response(user1Mention, user2Mention) },
        Color(165, 255, 76),
        Emotes.LoriHi
    )

    val SLAP_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::slap,
        RetributeSlapButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Slap.Response(user1Mention, user2Mention) },
        Color(244, 67, 54),
        Emotes.LoriHi // TODO: Emoji
    )

    val ATTACK_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::attack,
        RetributeAttackButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Attack.Response(user1Mention, user2Mention) },
        Color(244, 67, 54),
        Emotes.LoriHi // TODO: Emoji
    )

    val DANCE_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::dance,
        RetributeDanceButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Dance.Response(user1Mention, user2Mention) },
        Color(244, 67, 54), // TODO: Color
        Emotes.LoriHi // TODO: Emoji
    )

    val KISS_ATTRIBUTES = RoleplayActionAttributes(
        RandomRoleplayPicturesClient::kiss,
        RetributeKissButtonExecutor,
        { user1Mention, user2Mention -> I18nKeysData.Commands.Command.Roleplay.Kiss.Response(user1Mention, user2Mention) },
        Color(244, 67, 54), // TODO: Color
        Emotes.LoriKiss
    )

    suspend fun roleplayStuff(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        data: RetributeRoleplayData,
        client: RandomRoleplayPicturesClient,
        roleplayActionAttributes: RoleplayActionAttributes
    ): MessageCreateBuilder.() -> (Unit) {
        val (_, giver, receiver) = data

        val gender1 = loritta.services.users.getOrCreateUserProfile(UserId(giver)).getProfileSettings().gender
        val gender2 = loritta.services.users.getOrCreateUserProfile(UserId(receiver)).getProfileSettings().gender

        val result = roleplayActionAttributes.actionBlock.invoke(
            client,
            when (gender1) {
                Gender.MALE -> net.perfectdreams.randomroleplaypictures.common.Gender.MALE
                Gender.FEMALE -> net.perfectdreams.randomroleplaypictures.common.Gender.FEMALE
                Gender.UNKNOWN -> net.perfectdreams.randomroleplaypictures.common.Gender.UNKNOWN
            },
            when (gender2) {
                Gender.MALE -> net.perfectdreams.randomroleplaypictures.common.Gender.MALE
                Gender.FEMALE -> net.perfectdreams.randomroleplaypictures.common.Gender.FEMALE
                Gender.UNKNOWN -> net.perfectdreams.randomroleplaypictures.common.Gender.UNKNOWN
            }
        )

        val (picturePath, pictureSource) = result

        return {
            embed {
                description = "${roleplayActionAttributes.embedEmoji} **${i18nContext.get(roleplayActionAttributes.embedResponse.invoke(mentionUser(giver), mentionUser(receiver)))}**"

                image = client.baseUrl + "/img/$picturePath"

                color = roleplayActionAttributes.embedColor
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    "Retribuir",
                    roleplayActionAttributes.retributionButtonDeclaration,
                    ComponentDataUtils.encode(
                        RetributeRoleplayData(
                            receiver,
                            receiver,
                            giver
                        )
                    )
                )

                if (pictureSource != null) {
                    interactiveButton(
                        ButtonStyle.Secondary,
                        "Fonte",
                        SourcePictureExecutor,
                        ComponentDataUtils.encode<PictureSource>(pictureSource)
                    )
                } else {
                    interactionButton(
                        ButtonStyle.Secondary,
                        "dummy"
                    ) {
                        label = "Fonte"
                        disabled = true
                    }
                }
            }
        }
    }
}