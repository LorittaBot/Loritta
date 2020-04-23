package net.perfectdreams.loritta.plugin.helpinghands.utils

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
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
	const val DAY_THRESHOLD = 7L

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
		val nowSevenDaysAgo = LocalDateTime.now()
				.atOffset(ZoneOffset.UTC)
				.minusDays(DAY_THRESHOLD)
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
						Dailies.receivedAt lessEq nowSevenDaysAgo and (Profiles.money greaterEq 100_000L) and (
								receivedBy.notInSubQuery(
										Dailies.slice(receivedBy).select {
											Dailies.receivedAt greaterEq nowSevenDaysAgo
										}.groupBy(receivedBy)
								)
								)
					}
					.groupBy(receivedBy, money)
					.also { logger.info { "There are ${it.count()} inactive daily users!" } }
					.toList()
		}

		inactiveUsers.forEach {
			val userId = it[receivedBy]

			val removeMoney = (it[money] * 0.05).toLong()

			logger.info { "Removing $removeMoney from $userId, current total is ${it[Profiles.money]}" }

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
	}

	infix fun <T> Expression<T>.notInSubQuery(query: Query): NotInSubQueryOp<T> = NotInSubQueryOp(this, query)

	class NotInSubQueryOp<T>(val expr: Expression<T>, val query: Query): Op<Boolean>() {
		override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
			append(expr, " NOT IN (")
			query.prepareSQL(this)
			+")"
		}
	}
}