package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.UserSettings
import org.jetbrains.exposed.dao.*

class ProfileSettings(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<ProfileSettings>(UserSettings)

	val userId = this.id.value
	var aboutMe by UserSettings.aboutMe
	var gender by UserSettings.gender
	var hidePreviousUsernames by UserSettings.hidePreviousUsernames
	var hideSharedServers by UserSettings.hideSharedServers
	var hideLastSeen by UserSettings.hideLastSeen
}