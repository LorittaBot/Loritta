package com.mrpowergamerbr.loritta.userdata

data class StarboardConfig (
	var isEnabled: Boolean,
	var starboardId: String?) {
	constructor() : this(false, null)
}