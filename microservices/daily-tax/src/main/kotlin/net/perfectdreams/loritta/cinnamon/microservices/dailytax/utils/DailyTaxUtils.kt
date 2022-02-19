package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDateTime
import java.time.ZoneOffset

object DailyTaxUtils {
    val THRESHOLDS = listOf(
        DailyTaxThreshold(
            3L,
            100_000_000L,
            0.5
        ),
        DailyTaxThreshold(
            7L,
            10_000_000L,
            0.25
        ),
        DailyTaxThreshold(
            14L,
            1_000_000L,
            0.1
        ),
        DailyTaxThreshold(
            30L,
            100_000L,
            0.05
        )
    )

    fun doSomething(dayOffset: Long, block: (threshold: DailyTaxThreshold, inactiveDailyUser: InactiveDailyUser) -> (Unit)) {
        val moneySum = Payments.money.sum()

        val usersToBeIgnored = Payments.slice(Payments.userId, moneySum).select {
            Payments.expiresAt greaterEq System.currentTimeMillis()
        }.groupBy(Payments.userId)
            .having { moneySum greaterEq 90.00 } // It is actually 99.99 but shhhhh
            .map { it[Payments.userId] }
            .toMutableSet()

        val processedUsers = mutableSetOf<Long>()

        for (threshold in THRESHOLDS.sortedByDescending { it.minimumSonhosForTrigger }) {
            val nowXDaysAgo = LocalDateTime.now()
                .atOffset(ZoneOffset.UTC)
                .plusDays(dayOffset)
                .minusDays(threshold.maxDayThreshold)
                .toInstant()
                .toEpochMilli()

            val affectedProfiles = mutableListOf<InactiveDailyUser>()

            TransactionManager.current().exec(
                """SELECT profiles.id, profiles.money FROM profiles LEFT JOIN LATERAL (SELECT received_at FROM dailies WHERE profiles.id = dailies.received_by AND dailies.received_at > $nowXDaysAgo LIMIT 1) AS a ON TRUE WHERE profiles.money >= ${threshold.minimumSonhosForTrigger} AND a.received_at IS NULL;"""
            ) { rs ->
                while (rs.next()) {
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
                }
            }

            affectedProfiles.filter { it.id !in usersToBeIgnored && it.id !in processedUsers }.forEach { inactiveDailyUser ->
                block.invoke(threshold, inactiveDailyUser)
            }

            processedUsers.addAll(affectedProfiles.map { it.id })
        }
    }

    data class DailyTaxThreshold(
        val maxDayThreshold: Long,
        val minimumSonhosForTrigger: Long,
        val tax: Double
    )

    data class InactiveDailyUser(
        val id: Long,
        val money: Long,
        val moneyToBeRemoved: Long
    )
}