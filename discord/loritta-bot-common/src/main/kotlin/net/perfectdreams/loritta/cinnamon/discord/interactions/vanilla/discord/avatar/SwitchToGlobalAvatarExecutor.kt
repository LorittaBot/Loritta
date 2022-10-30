package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.morenitta.LorittaBot

class SwitchToGlobalAvatarExecutor(loritta: LorittaBot, val lorittaId: Snowflake) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(
        SwitchToGlobalAvatarExecutor::class,
        ComponentExecutorIds.SWITCH_TO_GLOBAL_AVATAR_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decodedInteractionData =
            context.decodeDataFromComponentAndRequireUserToMatch<UserDataUtils.SwitchAvatarInteractionIdData>()
        val data = UserDataUtils.getInteractionDataOrRetrieveViaRestIfItDoesNotExist(
            loritta,
            decodedInteractionData,
            false
        )

        val newData = UserDataUtils.ViewingGlobalUserAvatarData(
            data.userName,
            data.discriminator,
            data.userAvatarId,
            data.memberAvatarId
        )

        val message = UserDataUtils.createAvatarPreviewMessage(
            loritta,
            context.i18nContext,
            lorittaId,
            decodedInteractionData,
            newData
        )

        when (decodedInteractionData.targetType) {
            MessageTargetType.SEND_MESSAGE_PUBLIC -> context.sendMessage {
                message()
            }

            MessageTargetType.SEND_MESSAGE_EPHEMERAL -> context.sendEphemeralMessage {
                message()
            }

            MessageTargetType.EDIT_MESSAGE -> context.updateMessage {
                message()
            }
        }
    }
}