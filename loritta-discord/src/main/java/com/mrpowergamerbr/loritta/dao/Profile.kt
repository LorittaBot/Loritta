package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.utils.TakingMoreSonhosThanAllowedException
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.RuntimeException
import java.util.*

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles)

	val userId = this.id.value
	var xp by Profiles.xp
	var money by Profiles.money
	var lastMessageSentAt by Profiles.lastMessageSentAt
	var lastMessageSentHash by Profiles.lastMessageSentHash
	var lastCommandSentAt by Profiles.lastCommandSentAt
	var isAfk by Profiles.isAfk
	var afkReason by Profiles.afkReason

	var settings by ProfileSettings referencedOn Profiles.settings
	var marriage by Marriage optionalReferencedOn Profiles.marriage

	/**
	 * Returns if the user can get a daily
	 *
	 * @return the result and when the user can get the daily again
	 */
	fun canGetDaily(): Pair<Boolean, Long> {
		val receivedDailyAt = transaction(Databases.loritta) {
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
			BannedUsers.select { BannedUsers.userId eq this@Profile.id.value }
					.orderBy(BannedUsers.bannedAt, SortOrder.DESC)
					.firstOrNull()
		} ?: return null

		if (bannedState[BannedUsers.valid] && bannedState[BannedUsers.expiresAt] ?: Long.MAX_VALUE >= System.currentTimeMillis())
			return bannedState

		return null
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

		// If everything went well, refresh the current DAO
		if (refreshOnSuccess)
			this@Profile.refresh()
	}

	suspend fun getProfileBackground() = loritta.getUserProfileBackground(userId)

	class XpWrapper constructor(val currentLevel: Int, val expLeft: Long)
}