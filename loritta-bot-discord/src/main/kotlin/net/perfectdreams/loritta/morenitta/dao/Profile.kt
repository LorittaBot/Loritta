package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.TakingMoreSonhosThanAllowedException
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.ZonedDateTime

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles) {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	val userId = this.id.value
	var xp by Profiles.xp
	var money by Profiles.money
	var lastMessageSentAt by Profiles.lastMessageSentAt
	var lastMessageSentHash by Profiles.lastMessageSentHash
	var lastCommandSentAt by Profiles.lastCommandSentAt
	var isAfk by Profiles.isAfk
	var afkReason by Profiles.afkReason
	var vacationUntil by Profiles.vacationUntil

	var settings by ProfileSettings referencedOn Profiles.settings

	/**
	 * Returns if the user can get a daily
	 *
	 * @return the result and when the user can get the daily again
	 */
	suspend fun canGetDaily(loritta: LorittaBot): Pair<Boolean, Long> {
		val receivedDailyAt = loritta.newSuspendedTransaction {
			Dailies.selectAll().where { Dailies.receivedById eq userId }
					.orderBy(Dailies.receivedAt, SortOrder.DESC)
					.limit(1)
					.firstOrNull()
		}?.get(Dailies.receivedAt) ?: 0L

		val tomorrow = ZonedDateTime.ofInstant(Instant.ofEpochMilli(receivedDailyAt), Constants.LORITTA_TIMEZONE)
			.plusDays(1)
			.withHour(0)
			.withMinute(0)
			.withSecond(0)
			.withNano(0)

		val tomorrowInEpochMillis = tomorrow.toEpochSecond() * 1000

		return Pair(System.currentTimeMillis() > tomorrowInEpochMillis, tomorrowInEpochMillis)
	}

	/**
	 * Get the user's current banned state, if it exists and if it is valid
	 */
	suspend fun getBannedState(loritta: LorittaBot): ResultRow? {
		val bannedState = loritta.newSuspendedTransaction {
			BannedUsers.selectAll().where {
				BannedUsers.userId eq this@Profile.id.value and
						(BannedUsers.valid eq true) and
						(
								BannedUsers.expiresAt.isNull()
										or
										(
												BannedUsers.expiresAt.isNotNull() and
														(BannedUsers.expiresAt greaterEq System.currentTimeMillis()))
								)

			}
					.orderBy(BannedUsers.bannedAt, SortOrder.DESC)
					.firstOrNull()
		} ?: return null

		return bannedState
	}

	fun getCurrentLevel(): XpWrapper {
		return XpWrapper((xp / 1000).toInt(), xp)
	}

	/**
	 * Adds sonhos to the profile
	 */
	fun addSonhosNested(
			quantity: Long,
			refreshBeforeAction: Boolean = true,
			checksBeforeAction: ((Profile) -> (Boolean))? = null,
			refreshOnSuccess: Boolean = true
	) {
		if (!PaymentUtils.economyEnabled)
			throw PaymentUtils.EconomyDisabledException()

		val id = id

		if (refreshBeforeAction)
			this@Profile.refresh()

		if (checksBeforeAction?.invoke(this@Profile) == false)
			return

		Profiles.update({ Profiles.id eq id }) {
			with(SqlExpressionBuilder) {
				it[Profiles.money] = Profiles.money + quantity
			}
		}
		logger.info { "Added $quantity sonhos to ${id.value}" }

		// If everything went well, refresh the current DAO
		if (refreshOnSuccess)
			this@Profile.refresh()
	}

	/**
	 * Takes sonhos from the profile
	 */
	fun takeSonhosNested(
			quantity: Long,
			refreshBeforeAction: Boolean = true,
			failIfQuantityIsSmallerThanWhatUserHas: Boolean = true,
			checksBeforeAction: ((Profile) -> (Boolean))? = null,
			refreshOnSuccess: Boolean = true
	) {
		if (!PaymentUtils.economyEnabled)
			throw PaymentUtils.EconomyDisabledException()

		val id = id

		if (refreshBeforeAction)
			this@Profile.refresh()

		if (failIfQuantityIsSmallerThanWhatUserHas && quantity > this@Profile.money)
			throw TakingMoreSonhosThanAllowedException()

		if (checksBeforeAction?.invoke(this@Profile) == false)
			return

		Profiles.update({ Profiles.id eq id }) {
			with(SqlExpressionBuilder) {
				it[Profiles.money] = Profiles.money - quantity
			}
		}

		logger.info { "Took $quantity sonhos from ${id.value}" }

		// If everything went well, refresh the current DAO
		if (refreshOnSuccess)
			this@Profile.refresh()
	}

	/**
	 * Add sonhos and adds to the transaction log
	 */
	fun addSonhosAndAddToTransactionLogNested(
		quantity: Long,
		reason: SonhosPaymentReason,
		givenAtMillis: Long = System.currentTimeMillis(),
		refreshBeforeAction: Boolean = true,
		checksBeforeAction: ((Profile) -> (Boolean))? = null,
		refreshOnSuccess: Boolean = true
	) {
		addSonhosNested(
			quantity,
			refreshBeforeAction,
			checksBeforeAction,
			refreshOnSuccess
		)

		PaymentUtils.addToTransactionLogNested(
			quantity,
			reason,
			id.value,
			null,
			givenAtMillis
		)
	}

	/**
	 * Takes sonhos and adds to the transaction log
	 */
	fun takeSonhosAndAddToTransactionLogNested(
		quantity: Long,
		reason: SonhosPaymentReason,
		givenAtMillis: Long = System.currentTimeMillis(),
		refreshBeforeAction: Boolean = true,
		failIfQuantityIsSmallerThanWhatUserHas: Boolean = true,
		checksBeforeAction: ((Profile) -> (Boolean))? = null,
		refreshOnSuccess: Boolean = true
	) {
		takeSonhosNested(
			quantity,
			refreshBeforeAction,
			failIfQuantityIsSmallerThanWhatUserHas,
			checksBeforeAction,
			refreshOnSuccess
		)

		PaymentUtils.addToTransactionLogNested(
			quantity,
			reason,
			null,
			id.value,
			givenAtMillis
		)
	}

	suspend fun getProfileBackground(loritta: LorittaBot) = loritta.profileDesignManager.getUserProfileBackground(userId)

	class XpWrapper constructor(val currentLevel: Int, val expLeft: Long)
}