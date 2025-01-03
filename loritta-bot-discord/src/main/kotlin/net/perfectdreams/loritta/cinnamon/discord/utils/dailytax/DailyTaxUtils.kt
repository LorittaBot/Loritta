package net.perfectdreams.loritta.cinnamon.discord.utils.dailytax

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.common.utils.DailyTaxThresholds.THRESHOLDS
import net.perfectdreams.loritta.common.utils.PendingImportantNotificationState
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

object DailyTaxUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets and processes inactive daily users
     *
     * @param bypassDailyTaxUserIds the IDs of users that can bypass the daily tax
     * @param dayOffset             offsets (plusDays) the current day by [dayOffset]
     * @param block                 block that will be executed when a inactive daily user is found
     */
    fun getAndProcessInactiveDailyUsers(bypassDailyTaxUserIds: List<Long>, dayOffset: Long, block: (threshold: DailyTaxThresholds.DailyTaxThreshold, inactiveDailyUser: InactiveDailyUser) -> (Unit)) {
        // This looks weird, but that's because this list is mutated down below
        val usersToBeIgnored = mutableListOf<Long>()
        usersToBeIgnored.addAll(bypassDailyTaxUserIds)

        for (threshold in THRESHOLDS.sortedByDescending { it.minimumSonhosForTrigger }) {
            logger.info { "Checking daily inactivity tax threshold $threshold" }

            val nowXDaysAgo = LocalDateTime.now()
                .atOffset(ZoneOffset.UTC)
                .plusDays(dayOffset)
                .minusDays(threshold.maxDayThreshold.toLong())
                .toInstant()
                .toEpochMilli()

            val affectedProfiles = mutableListOf<InactiveDailyUser>()

            TransactionManager.current().exec(
                """SELECT profiles.id, profiles.money FROM profiles LEFT JOIN LATERAL (SELECT received_at FROM dailies WHERE profiles.id = dailies.received_by AND dailies.received_at > $nowXDaysAgo LIMIT 1) AS a ON TRUE WHERE profiles.money >= ${threshold.minimumSonhosForTrigger} AND a.received_at IS NULL;"""
            ) { rs ->
                while (rs.next()) {
                    try {
                        val profileId = rs.getLong(1)
                        val money = rs.getLong(2)
                        val moneyToBeRemoved = (money * threshold.tax).toLong()
                        affectedProfiles.add(
                            InactiveDailyUser(
                                profileId,
                                money,
                                moneyToBeRemoved
                            )
                        )
                    } catch (e: Exception) {
                        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.postgresql.util.PSQLException: Bad value for type long : 9.223372036854776e+18
                        logger.warn(e) { "Exception while trying to read ResultSet, skipping entry..." }
                    }
                }
            }

            logger.info { "There are ${affectedProfiles.size} affected profiles (including users to be ignored) for the threshold $threshold!" }

            affectedProfiles.filter { it.id !in usersToBeIgnored }.forEach { inactiveDailyUser ->
                logger.info { "Processing inactive daily user $inactiveDailyUser in the tax threshold $threshold" }
                block.invoke(threshold, inactiveDailyUser)
            }

            usersToBeIgnored.addAll(affectedProfiles.map { it.id })
        }
    }

    fun insertImportantNotification(inactiveDailyUser: InactiveDailyUser, notificationId: Long) {
        PendingImportantNotifications.insert {
            it[PendingImportantNotifications.userId] = inactiveDailyUser.id
            it[PendingImportantNotifications.state] = PendingImportantNotificationState.PENDING
            it[PendingImportantNotifications.notification] = notificationId
            it[PendingImportantNotifications.submittedAt] = Instant.now()
        }
    }

    data class InactiveDailyUser(
        val id: Long,
        val money: Long,
        val moneyToBeRemoved: Long
    )
}