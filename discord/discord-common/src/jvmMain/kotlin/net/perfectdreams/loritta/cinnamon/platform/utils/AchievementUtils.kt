package net.perfectdreams.loritta.cinnamon.platform.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.BarebonesInteractionContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object AchievementUtils {
    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementToUser(
        loritta: LorittaCinnamon,
        context: BarebonesInteractionContext,
        i18nContext: I18nContext,
        userId: UserId,
        type: AchievementType,
        achievedAt: Instant = Clock.System.now()
    ) {
        val profile = loritta.services.users.getOrCreateUserProfile(userId)
        val wasAchievementGiven = profile.giveAchievement(
            type,
            achievedAt
        )

        if (wasAchievementGiven)
            context.sendEphemeralMessage {
                styled(
                    content = "**${i18nContext.get(I18nKeysData.Achievements.AchievementUnlocked)}**",
                    prefix = Emotes.Sparkles
                )

                styled(
                    "**${i18nContext.get(type.title)}:** ${i18nContext.get(type.description)}",
                    prefix = type.category.emote
                )

                styled(
                    i18nContext.get(I18nKeysData.Achievements.ViewYourAchievements("/achievements")),
                    prefix = Emotes.LoriWow
                )
            }
    }
}