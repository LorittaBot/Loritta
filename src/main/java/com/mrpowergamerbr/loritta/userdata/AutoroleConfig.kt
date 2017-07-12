package com.mrpowergamerbr.loritta.userdata

data class AutoroleConfig (
	var isEnabled: Boolean,
	var roles: MutableList<String>) {
	constructor() : this(false, mutableListOf<String>())
}