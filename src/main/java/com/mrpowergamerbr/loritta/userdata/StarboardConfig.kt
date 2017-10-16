package com.mrpowergamerbr.loritta.userdata

data class StarboardConfig (
	var isEnabled: Boolean,
	var starboardId: String?,
	var requiredStars: Int) {
	constructor() : this(false, null, 1)
}