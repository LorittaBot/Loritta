package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayActionAttributes
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

abstract class RetributePictureExecutor(
    loritta: LorittaCinnamon,
    private val client: RandomRoleplayPicturesClient,
    private val attributes: RoleplayActionAttributes,
) : CinnamonButtonExecutor(loritta) {
    override suspend fun onClick(user: User, context: ComponentContext) {
        val retributionData = context.decodeDataFromComponentAndRequireUserToMatch<RetributeRoleplayData>()

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