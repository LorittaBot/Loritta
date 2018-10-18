package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class Profile(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, Profile>(Profiles)

	val userId = this.id.value
	var xp by Profiles.xp
	var money by Profiles.money
	var isBanned by Profiles.isBanned
	var bannedReason by Profiles.bannedReason
	var lastMessageSentAt by Profiles.lastMessageSentAt
	var lastMessageSentHash by Profiles.lastMessageSentHash
	var isAfk by Profiles.isAfk
	var afkReason by Profiles.afkReason
	var isDonator by Profiles.isDonator
	var donatorPaid by Profiles.donatorPaid
	var donatedAt by Profiles.donatedAt
	var donationExpiresIn by Profiles.donationExpiresIn
	var marriedWith by Profiles.marriedWith
	var marriedAt by Profiles.marriedAt

	var settings by ProfileSettings referencedOn Profiles.settings
}