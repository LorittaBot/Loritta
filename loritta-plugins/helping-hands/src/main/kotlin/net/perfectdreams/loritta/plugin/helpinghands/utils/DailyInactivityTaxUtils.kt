package net.perfectdreams.loritta.plugin.helpinghands.utils

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

object DailyInactivityTaxUtils {
	private val logger = KotlinLogging.logger {}
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

	internal fun createAutoInactivityTask(): suspend CoroutineScope.() -> Unit = {
		while (true) {
			val midnight = LocalTime.MIDNIGHT
			val today = LocalDate.now(ZoneOffset.UTC)
			val todayMidnight = LocalDateTime.of(today, midnight)
			val tomorrowMidnight = todayMidnight.plusDays(1)
			val diff = tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

			logger.info { "Waiting ${diff}ms until UTC midnight for the daily inactivity task..." }
			delay(diff)

			try {
				runDailyInactivityTax()
			} catch (e: Exception) {
				logger.error(e) { "Something went wrong while running the daily inactivity task!" }
			}
		}
	}

	fun runDailyInactivityTax() {
		logger.info { "Running the daily inactivity tax!" }

		runDailyInactivityForUsersThatCollectedDailyBefore()
		runDailyInactivityForUsersThatNeverCollectedDailyBefore()
	}

	private fun runDailyInactivityForUsersThatCollectedDailyBefore() {
		logger.info { "Running the daily inactivity for users that collected daily before!" }

		val processedUsers = mutableSetOf(
				// lori so cute she doesn't deserve to get daily every single day
				loritta.discordConfig.discord.clientId.toLong()
		)

		for (threshold in THRESHOLDS) {
			logger.info { "Checking daily inactivity tax threshold $threshold" }

			val nowXDaysAgo = LocalDateTime.now()
					.atOffset(ZoneOffset.UTC)
					.minusDays(threshold.maxDayThreshold)
					.toInstant()
					.toEpochMilli()

			val receivedBy = Dailies.receivedById
			val money = Profiles.money

			// Feito de forma "separada" para evitar erros de concurrent updates, se um falhar, não vai fazer rollback na transação inteira
			val inactiveUsers = transaction(Databases.loritta) {
				// select dailies.received_by, profiles.money from dailies inner join profiles on profiles.id = dailies.received_by where received_at < 1587178800000 and received_by not in (select received_by from dailies where received_at > 1587178800000 group by received_by) group by received_by, money order by money desc;

				// (select received_by from dailies where received_at > 1587178800000 group by received_by)
				Dailies.join(Profiles, JoinType.INNER, Dailies.receivedById, Profiles.id)
						.slice(receivedBy, money)
						.select {
							Dailies.receivedAt lessEq nowXDaysAgo and (Profiles.money greaterEq threshold.minimumSonhosForTrigger) and (
									receivedBy.notInSubQuery(
											Dailies.slice(receivedBy).select {
												Dailies.receivedAt greaterEq nowXDaysAgo
											}.groupBy(receivedBy)
									)
									)
						}
						.groupBy(receivedBy, money)
						.toList()
						// We display the inactive daily users after the ".toList()" because, if it is placed before, two queries will
						// be made: One for the query itself and then another for the Exposed ".count()" call.
						.also { logger.info { "There are ${it.size} inactive daily users!" } }
			}

			inactiveUsers.filter { it[receivedBy] !in processedUsers }.forEach {
				val userId = it[receivedBy]

				val removeMoney = (it[money] * threshold.tax).toLong()

				logger.info { "Removing $removeMoney from $userId, using threshold tax $threshold, current total is ${it[Profiles.money]}" }

				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq userId }) {
						with(SqlExpressionBuilder) {
							it.update(Profiles.money, money - removeMoney)
						}
					}

					SonhosTransaction.insert {
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = removeMoney.toBigDecimal()
						it[reason] = SonhosPaymentReason.INACTIVE_DAILY_TAX
						it[givenBy] = userId
					}
				}
			}

			processedUsers += inactiveUsers.map { it[receivedBy] }
		}
	}

	private fun runDailyInactivityForUsersThatNeverCollectedDailyBefore() {
		// The query above does *not* match users that never got daily before, that's why we need to do this query too.
		// This query will only match users with >= threshold that *never* got daily before.
		logger.info { "Running the daily inactivity for users that never collected daily before!" }

		val processedUsers = mutableSetOf(
				// lori so cute she doesn't deserve to get daily every single day
				loritta.discordConfig.discord.clientId.toLong()
		)

		for (threshold in THRESHOLDS) {
			logger.info { "Checking daily inactivity tax threshold $threshold" }

			val receivedBy = Profiles.id
			val money = Profiles.money

			// Feito de forma "separada" para evitar erros de concurrent updates, se um falhar, não vai fazer rollback na transação inteira
			val inactiveUsers = transaction(Databases.loritta) {
				Profiles.join(Dailies, JoinType.LEFT, Profiles.id, Dailies.receivedById)
						.select {
							(Profiles.money greaterEq threshold.minimumSonhosForTrigger) and
									Dailies.id.isNull()
						}
						.groupBy(receivedBy, money, Dailies.id)
						.toList()
						// We display the inactive daily users after the ".toList()" because, if it is placed before, two queries will
						// be made: One for the query itself and then another for the Exposed ".count()" call.
						.also { logger.info { "There are ${it.size} inactive daily users that never got a daily before!" } }
			}

			inactiveUsers.filter { it[receivedBy].value !in processedUsers }.forEach {
				val userId = it[receivedBy].value

				val removeMoney = (it[money] * threshold.tax).toLong()

				logger.info { "Removing $removeMoney from $userId (that has never got a daily before!), using threshold tax $threshold, current total is ${it[Profiles.money]}" }

				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq userId }) {
						with(SqlExpressionBuilder) {
							it.update(Profiles.money, money - removeMoney)
						}
					}

					SonhosTransaction.insert {
						it[givenAt] = System.currentTimeMillis()
						it[quantity] = removeMoney.toBigDecimal()
						it[reason] = SonhosPaymentReason.INACTIVE_DAILY_TAX
						it[givenBy] = userId
					}
				}
			}

			processedUsers += inactiveUsers.map { it[receivedBy].value }
		}
	}

	data class DailyTaxThreshold(
			val maxDayThreshold: Long,
			val minimumSonhosForTrigger: Long,
			val tax: Double
	)
}