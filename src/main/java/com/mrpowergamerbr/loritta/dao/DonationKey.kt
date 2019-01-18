package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.DonationKeys
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class DonationKey(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<DonationKey>(DonationKeys)

	var userId by DonationKeys.userId
    var value by DonationKeys.value
    var expiresAt by DonationKeys.expiresAt

    /**
     * Returns if the key is still active
     */
    fun isActive() = expiresAt >= System.currentTimeMillis()
}