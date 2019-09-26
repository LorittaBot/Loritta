package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles)

	val userId = this.id.value
	var xp by Profiles.xp
	var money by Profiles.money
	var isBanned by Profiles.isBanned
	var bannedReason by Profiles.bannedReason
	var lastMessageSentAt by Profiles.lastMessageSentAt
	var lastMessageSentHash by Profiles.lastMessageSentHash
	var lastCommandSentAt by Profiles.lastCommandSentAt
	var isAfk by Profiles.isAfk
	var afkReason by Profiles.afkReason
	var isDonator by Profiles.isDonator
	var donatorPaid by Profiles.donatorPaid
	var donatedAt by Profiles.donatedAt
	var donationExpiresIn by Profiles.donationExpiresIn

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
					.orderBy(Dailies.receivedAt to false)
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
	 * Returns if the user is an active donator
	 */
	fun isActiveDonator() = isDonator && donationExpiresIn > System.currentTimeMillis()
}