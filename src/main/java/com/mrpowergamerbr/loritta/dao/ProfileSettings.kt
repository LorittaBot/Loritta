package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.ProfilesSettings
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class ProfileSettings(id: EntityID<Long>) : Entity<Long>(id) {
	companion object : EntityClass<Long, ProfileSettings>(ProfilesSettings)

	val userId = this.id.value
	var aboutMe by ProfilesSettings.aboutMe
	var gender by ProfilesSettings.gender
	var hidePreviousUsernames by ProfilesSettings.hidePreviousUsernames
	var hideSharedServers by ProfilesSettings.hideSharedServers
	var hideLastSeen by ProfilesSettings.hideLastSeen
}