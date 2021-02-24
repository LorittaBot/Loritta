package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Daily
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.tables.BannedUsers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId

object AccountUtils {
    /**
     * Gets the user's last received daily reward
     *
     * @param profile   the user's profile
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(profile: Profile, afterTime: Long = Long.MIN_VALUE): Daily? {
        return loritta.newSuspendedTransaction {
            val dailyResult = Dailies.select {
                Dailies.receivedById eq profile.id.value and (Dailies.receivedAt greaterEq afterTime)
            }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .firstOrNull()

            if (dailyResult != null)
                Daily.wrapRow(dailyResult)
            else null
        }
    }

    /**
     * Gets the user's received daily reward from today, or null, if the user didn't get the daily reward today
     *
     * @param profile the user's profile
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserTodayDailyReward(profile: Profile) = getUserDailyRewardInTheLastXDays(profile, 0)

    /**
     * Gets the user's received daily reward from the last [dailyInThePreviousDays] days, or null, if the user didn't get the daily reward in the specified threshold
     *
     * @param profile the user's profile
     * @param dailyInThePreviousDays the daily minimum days threshold
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserDailyRewardInTheLastXDays(profile: Profile, dailyInThePreviousDays: Long): Daily? {
        val dayAtMidnight = Instant.now()
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .toOffsetDateTime()
            .minusDays(dailyInThePreviousDays)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()

        return getUserLastDailyRewardReceived(profile, dayAtMidnight)
    }

    suspend fun checkAndSendMessageIfUserIsBanned(context: CommandContext, userProfile: Profile): Boolean {
        val bannedState = userProfile.getBannedState()
        val locale = context.locale

        if (bannedState != null) {
            val bannedAt = bannedState[BannedUsers.bannedAt]
            val bannedAtDiff = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(bannedAt, locale)
            val banExpiresAt = bannedState[BannedUsers.expiresAt]
            val responses = mutableListOf(
                LorittaReply(
                    "<@${userProfile.userId}> está **banido**",
                    "\uD83D\uDE45"
                ),
                LorittaReply(
                    "**Motivo:** `${bannedState[BannedUsers.reason]}`",
                    "✍",
                    mentionUser = false
                ),
                LorittaReply(
                    "**Data do Banimento:** `$bannedAtDiff`",
                    "⏰",
                    mentionUser = false
                )
            )

            if (banExpiresAt != null) {
                val banDurationDiff = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(banExpiresAt, locale)
                responses.add(
                    LorittaReply(
                        "**Duração do banimento:** `$banDurationDiff`",
                        "⏳",
                        mentionUser = false
                    )
                )
            }

            context.reply(*responses.toTypedArray())
            return true
        }
        return false
    }
}