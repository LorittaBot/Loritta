package com.mrpowergamerbr.loritta.oauth2

/**
 * Holds information about the user, this is used to avoid hitting Discord's endpoints for no reason.
 */
open class SimpleUserIdentification(
		val username: String,
		val id: String,
		val avatar: String,
		val discriminator: String
)