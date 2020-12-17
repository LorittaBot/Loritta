package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.UserDonationKeys
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserDonationKey(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<UserDonationKey>(Profiles)

	var userId by UserDonationKeys.userId
    var value by UserDonationKeys.value
    var expiresAt by UserDonationKeys.expiresAt
	var metadata by UserDonationKeys.metadata
	val activeIn by UserDonationKey optionalReferencedOn UserDonationKeys.activeIn

    /**
     * Returns if the key is still active
     */
    fun isActive() = expiresAt >= System.currentTimeMillis()
}