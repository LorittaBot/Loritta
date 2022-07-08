package net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

class SwitchToGuildProfileAvatarExecutor(val loritta: LorittaCinnamon, val lorittaId: Snowflake) :
    ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.SWITCH_TO_GUILD_PROFILE_AVATAR_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val decodedInteractionData = context.decodeDataFromComponentAndRequireUserToMatch<UserDataUtils.SwitchAvatarInteractionIdData>(data)
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