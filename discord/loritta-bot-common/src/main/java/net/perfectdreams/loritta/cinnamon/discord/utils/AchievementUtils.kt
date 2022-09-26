package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.BarebonesInteractionContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object AchievementUtils {
    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, this method will return true, if else, false.
     *
     * This won't notify the user about the achievement, if you want the user to be notified, please see [giveAchievementToUserAndNotifyThem]
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementToUser(
        loritta: LorittaCinnamon,
        userId: UserId,
        type: AchievementType,
        achievedAt: Instant = Clock.System.now()
    ): Boolean {
        val profile = loritta.services.users.getOrCreateUserProfile(userId)
        return profile.giveAchievement(
            type,
            achievedAt
        )
    }

    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementToUserAndNotifyThem(
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
                    i18nContext.get(I18nKeysData.Achievements.ViewYourAchievements(loritta.commandMentions.achievements)),
                    prefix = Emotes.LoriWow
                )
            }
    }
}