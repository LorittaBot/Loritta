package net.perfectdreams.loritta.cinnamon.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.discordinteraktions.common.InteractionContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.platform.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

open class InteractionContext(
    val loritta: LorittaCinnamon,
    val i18nContext: I18nContext,
    val user: User,
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    override val interaKTionsContext: InteractionContext
) : BarebonesInteractionContext(interaKTionsContext) {
    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievement(type: AchievementType, achievedAt: Instant = Clock.System.now())
            = AchievementUtils.giveAchievementToUser(loritta, this, i18nContext, UserId(user.id.value), type, achievedAt)
}