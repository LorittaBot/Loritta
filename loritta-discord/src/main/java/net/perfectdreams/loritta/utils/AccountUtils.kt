package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.dao.Daily
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.loritta
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
}