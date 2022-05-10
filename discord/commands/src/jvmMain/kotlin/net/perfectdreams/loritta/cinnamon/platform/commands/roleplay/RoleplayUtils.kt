package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeRoleplayData
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source.SourcePictureExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureResponse
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureSource

object RoleplayUtils {
    val HUG_ATTRIBUTES = RoleplayActionAttributes(
        Color(255, 141, 230),
        Emotes.Blush
    )

    val HIGH_FIVE_ATTRIBUTES = RoleplayActionAttributes(
        Color(165, 255, 76),
        Emotes.LoriHi
    )

    val HEAD_PAT_ATTRIBUTES = RoleplayActionAttributes(
        Color(156, 39, 176),
        Emotes.LoriPat
    )

    suspend fun roleplayStuff(
        loritta: LorittaCinnamon,
        data: RetributeRoleplayData,
        client: RandomRoleplayPicturesClient,
        block: suspend RandomRoleplayPicturesClient.(net.perfectdreams.randomroleplaypictures.common.Gender, net.perfectdreams.randomroleplaypictures.common.Gender) -> (PictureResponse),
        retributionExecutorDeclaration: ButtonClickExecutorDeclaration,
        roleplayActionAttributes: RoleplayActionAttributes
    ): MessageCreateBuilder.() -> (Unit) {
        val (_, giver, receiver) = data

        val gender1 = loritta.services.users.getOrCreateUserProfile(UserId(giver)).getProfileSettings().gender
        val gender2 = loritta.services.users.getOrCreateUserProfile(UserId(receiver)).getProfileSettings().gender

        val result = block.invoke(
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
                description = "${roleplayActionAttributes.embedEmoji} <@${giver.value}> -> <@${receiver.value}>"

                image = client.baseUrl + "/img/$picturePath"

                color = roleplayActionAttributes.embedColor
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    "Retribuir",
                    retributionExecutorDeclaration,
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