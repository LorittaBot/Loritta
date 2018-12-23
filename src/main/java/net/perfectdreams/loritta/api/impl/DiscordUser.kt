package net.perfectdreams.loritta.api.impl

import net.perfectdreams.loritta.api.entities.User

class DiscordUser(val handle: net.dv8tion.jda.core.entities.User) : User {
	override val name = handle.name
	override val avatarUrl = handle.effectiveAvatarUrl
}