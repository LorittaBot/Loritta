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
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

			val money = Profiles.money

			val values = mutableListOf<InactiveDailyUser>()

			// Feito de forma "separada" para evitar erros de concurrent updates, se um falhar, não vai fazer rollback na transação inteira
			transaction(Databases.loritta) {
				// This is a *very* optimized query that seems to work fine and runs pretty fast
				// SELECT
				//    dailies.received_by, MAX(dailies.received_at), to_timestamp(MAX(dailies.received_at / 1000))
				//  FROM
				//    dailies
				//  WHERE
				//    dailies.received_by IN (SELECT profiles.id FROM profiles WHERE profiles.money >= 100000)
				//  GROUP BY dailies.received_by HAVING 1608396362303 >= MAX(dailies.received_at);
				//
				// The execution time is way smaller than the previous query that we were using (the previous one was using 4hrs just to get the inactive daily stuff!) while this one
				// takes a few minutes (max 5 minutes for the 100k sonhos threshold)
				//
				// Way better!
				//
				// We also need to do a INNER JOIN to get the Profiles' money (to avoid querying it later)
				// SELECT received_by, money FROM (
				//    SELECT dailies.received_by, MAX(dailies.received_at) FROM dailies WHERE dailies.received_by IN (SELECT profiles.id FROM profiles WHERE profiles.money >= 100000) GROUP BY dailies.received_by HAVING MAX(dailies.received_at) < 1608397009722
				// ) n1
				// INNER JOIN profiles ON id = received_by;
				//
				// Okay, I tried doing this with Exposed DSL but I didn't figure out a way to do the FROM ( ... ) part
				// So let's just hack around it because it doesn't really matter since a SQL Injection here is impossible

				TransactionManager.current().exec("""SELECT received_by, money FROM (
							SELECT dailies.received_by, MAX(dailies.received_at) FROM dailies WHERE dailies.received_by IN (SELECT profiles.id FROM profiles WHERE profiles.money >= ${threshold.minimumSonhosForTrigger}) GROUP BY dailies.received_by HAVING MAX(dailies.received_at) < $nowXDaysAgo
						) n1
						INNER JOIN profiles ON id = received_by;""") { rs ->
					while (rs.next()) {
						values.add(
								InactiveDailyUser(
										rs.getLong("received_by"),
										rs.getLong("money")
								)
						)
					}
				}
			}

			logger.info { "There are ${values.size} inactive daily users!" }

			values.filter { it.id !in processedUsers }.forEach {
				val userId = it.id

				val removeMoney = (it.money * threshold.tax).toLong()

				logger.info { "Removing $removeMoney from $userId, using threshold tax $threshold, current total is ${it.money}" }

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

			processedUsers += values.map { it.id }
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

	data class InactiveDailyUser(
			val id: Long,
			val money: Long
	)
}