package net.perfectdreams.loritta.cinnamon.discord.interactions

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.discordinteraktions.common.InteractionContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.locale.BaseLocale

open class InteractionContext(
    val loritta: LorittaBot,
    val i18nContext: I18nContext,
    @Deprecated("This is only provided for backward compatibility, to aid migration between the Cinnamon and Morenitta codebases. Please use i18nContext instead!")
    val locale: BaseLocale,
    val user: User,
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    override val interaKTionsContext: InteractionContext
) : BarebonesInteractionContext(interaKTionsContext) {
    val channelId: Snowflake
        get() = interaKTionsContext.channelId

    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementAndNotify(type: AchievementType, achievedAt: Instant = Clock.System.now())
            = AchievementUtils.giveAchievementToUserAndNotifyThem(loritta, this, i18nContext, UserId(user.id.value), type, achievedAt)
}