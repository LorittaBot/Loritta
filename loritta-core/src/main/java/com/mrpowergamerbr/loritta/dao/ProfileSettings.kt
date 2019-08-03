package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.UserSettings
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class ProfileSettings(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<ProfileSettings>(UserSettings)

	var aboutMe by UserSettings.aboutMe
	var gender by UserSettings.gender
	var hidePreviousUsernames by UserSettings.hidePreviousUsernames
	var hideSharedServers by UserSettings.hideSharedServers
	var hideLastSeen by UserSettings.hideLastSeen
	var activeProfile by UserSettings.activeProfile
	var boughtProfiles by UserSettings.boughtProfiles
	var birthday by UserSettings.birthday
}