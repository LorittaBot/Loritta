package net.perfectdreams.loritta.cinnamon.pudding.services

import java.time.Instant
import java.time.ZoneId
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingDaily
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class DailiesService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the user's last received daily reward
     *
     * @param userId   the user's id
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieve
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(userId: Long, afterTime: Long = Long.MIN_VALUE) =
        pudding.transaction {
            Dailies.select {
                (Dailies.receivedById eq userId) and (Dailies.receivedAt greaterEq afterTime)
            }.orderBy(Dailies.receivedAt to SortOrder.DESC).limit(1).firstOrNull()
                ?.let {
                    PuddingDaily.fromRow(it)
                }
        }

    /**
     * Gets the user's received daily reward from the last [dailyInThePreviousDays] days, or null, if the user didn't get the daily reward in the specified threshold
     *
     * @param userId the user's id
     * @param dailyInThePreviousDays the daily minimum days threshold
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserDailyRewardInTheLastXDays(userId: Long, dailyInThePreviousDays: Long): PuddingDaily? {
        val dayAtMidnight = Instant.now()
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .toOffsetDateTime()
            .minusDays(dailyInThePreviousDays)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()

        return getUserLastDailyRewardReceived(userId, dayAtMidnight)
    }


    /**
     * Gets the user's received daily reward from today, or null, if the user didn't get the daily reward today
     *
     * @param userId the user's id
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserTodayDailyReward(userId: Long) = getUserDailyRewardInTheLastXDays(userId, 0)
}