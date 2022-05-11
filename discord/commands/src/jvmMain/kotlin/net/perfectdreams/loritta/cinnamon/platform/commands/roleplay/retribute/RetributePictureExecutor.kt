package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayActionAttributes
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

abstract class RetributePictureExecutor(
    private val client: RandomRoleplayPicturesClient,
    private val attributes: RoleplayActionAttributes,
) : ButtonClickWithDataExecutor {
    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val retributionData = context.decodeViaComponentDataUtilsAndRequireUserToMatch<RetributeRoleplayData>(data)

        context.deferChannelMessage()

        val (achievementTargets, message) = RoleplayUtils.handleRoleplayMessage(
            context.loritta,
            context.i18nContext,
            retributionData,
            client,
            attributes
        )

        context.sendMessage(message)

        for ((achievementReceiver, achievement) in achievementTargets) {
            if (context.user.id == achievementReceiver)
                context.giveAchievementAndNotify(achievement)
            else
                AchievementUtils.giveAchievementToUser(context.loritta, UserId(achievementReceiver), achievement)
        }
    }
}