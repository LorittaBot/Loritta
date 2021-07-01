package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.TakingMoreSonhosThanAllowedException
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles) {
		private val logger = KotlinLogging.logger {}
	}

	val userId = this.id.value
	var xp by Profiles.xp
	var money by Profiles.money
	var lastMessageSentAt by Profiles.lastMessageSentAt
	var lastMessageSentHash by Profiles.lastMessageSentHash
	var lastCommandSentAt by Profiles.lastCommandSentAt
	var isAfk by Profiles.isAfk
	var afkReason by Profiles.afkReason

	var settings by ProfileSettings referencedOn Profiles.settings
	val marriageId by Profiles.marriage
	var marriage by Marriage optionalReferencedOn Profiles.marriage

	/**
	 * Returns if the user can get a daily
	 *
	 * @return the result and when the user can get the daily again
	 */
	suspend fun canGetDaily(): Pair<Boolean, Long> {
		val receivedDailyAt = loritta.newSuspendedTransaction {
			com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq userId }
					.orderBy(Dailies.receivedAt, SortOrder.DESC)
					.limit(1)
					.firstOrNull()
		}?.get(Dailies.receivedAt) ?: 0L


		val calendar = Calendar.getInstance()
		calendar.timeInMillis = receivedDailyAt
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val tomorrow = calendar.timeInMillis

		return Pair(System.currentTimeMillis() > tomorrow, tomorrow)
	}

	/**
	 * Get the user's current banned state, if it exists and if it is valid
	 */
	suspend fun getBannedState(): ResultRow? {
		val bannedState = loritta.newSuspendedTransaction {
			BannedUsers.select {
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

	suspend fun getProfileBackground() = loritta.getUserProfileBackground(userId)

	class XpWrapper constructor(val currentLevel: Int, val expLeft: Long)
}