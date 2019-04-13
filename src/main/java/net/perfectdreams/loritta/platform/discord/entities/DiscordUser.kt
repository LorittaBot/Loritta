package net.perfectdreams.loritta.platform.discord.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.api.entities.User

open class DiscordUser(@JsonIgnore val handle: net.dv8tion.jda.core.entities.User) : User {
	override val id: String
		get() = handle.id

	override val name: String
		get() = handle.name

	override val effectiveAvatarUrl: String
		get() = handle.effectiveAvatarUrl

	override val avatarUrl: String?
		get() = handle.avatarUrl

	override val asMention: String
		get() = handle.asMention

	override val isBot: Boolean
		get() = handle.isBot
}