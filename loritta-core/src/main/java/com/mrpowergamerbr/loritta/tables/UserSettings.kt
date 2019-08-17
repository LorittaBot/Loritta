package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.array
import com.mrpowergamerbr.loritta.utils.locale.Gender
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object UserSettings : LongIdTable() {
	val aboutMe = text("about_me").nullable()
	val gender = enumeration("gender", Gender::class)
	val hidePreviousUsernames = bool("hide_previous_usernames")
	val hideSharedServers = bool("hide_shared_servers")
	val hideLastSeen = bool("hide_last_seen")
	val activeProfile = text("active_profile").nullable()
	val boughtProfiles = array<String>("bought_profiles", TextColumnType())
	val birthday = date("birthday").nullable()
}