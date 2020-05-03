package com.mrpowergamerbr.loritta.userdata

@Deprecated("Migrated to PostgreSQL")
class StarboardConfig {
	var isEnabled: Boolean = false
	var starboardId: String? = null
	var requiredStars: Int = 1
}