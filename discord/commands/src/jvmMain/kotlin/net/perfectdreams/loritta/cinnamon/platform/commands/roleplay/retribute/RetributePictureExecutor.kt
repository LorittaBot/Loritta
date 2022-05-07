package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureResponse

abstract class RetributePictureExecutor(
    private val client: RandomRoleplayPicturesClient,
    private val block: suspend RandomRoleplayPicturesClient.(net.perfectdreams.randomroleplaypictures.common.Gender, net.perfectdreams.randomroleplaypictures.common.Gender) -> (PictureResponse),
    private val retributionButtonDeclaration: ButtonClickExecutorDeclaration
) : ButtonClickWithDataExecutor {
    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val retributionData = context.decodeViaComponentDataUtilsAndRequireUserToMatch<RetributeRoleplayData>(data)

        context.deferChannelMessage()

        context.sendMessage(
            RoleplayUtils.roleplayStuff(
                context.loritta,
                retributionData,
                client,
                block,
                retributionButtonDeclaration
            )
        )
    }
}