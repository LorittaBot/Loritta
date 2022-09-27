package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

class SwitchToGuildProfileAvatarExecutor(loritta: LorittaBot, val lorittaId: Snowflake) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.SWITCH_TO_GUILD_PROFILE_AVATAR_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decodedInteractionData = context.decodeDataFromComponentAndRequireUserToMatch<UserDataUtils.SwitchAvatarInteractionIdData>()
        val data = UserDataUtils.getInteractionDataOrRetrieveViaRestIfItDoesNotExist(
            loritta,
            decodedInteractionData,
            true
        )

        val newData = UserDataUtils.ViewingGuildProfileUserAvatarData(
            data.userName,
            data.discriminator,
            data.userAvatarId,
            data.memberAvatarId!! // at this point it should not be null
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