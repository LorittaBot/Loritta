package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.utils.Gender

class ProfileOptions {
	var aboutMe: String? = null
	var hidePreviousUsernames = false
	var hideSharedServers = false
	var hideLastSeen = false
	var gender = Gender.UNKNOWN
	var isAfk = false
	var afkReason: String? = null
}