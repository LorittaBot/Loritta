package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeRoleplayData
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureResponse

object RoleplayUtils {
    suspend fun roleplayStuff(
        loritta: LorittaCinnamon,
        data: RetributeRoleplayData,
        client: RandomRoleplayPicturesClient,
        block: suspend RandomRoleplayPicturesClient.(net.perfectdreams.randomroleplaypictures.common.Gender, net.perfectdreams.randomroleplaypictures.common.Gender) -> (PictureResponse),
        retributionExecutorDeclaration: ButtonClickExecutorDeclaration
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

        return {
            embed {
                description = "<@${giver.value}> -> <@${receiver.value}>"

                image = client.baseUrl + "/img/${result.path}"
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
            }
        }
    }
}