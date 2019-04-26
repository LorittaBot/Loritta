package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.api.entities.User

open class ParallaxUser(internal val user: User) {
	val avatar get() = user.avatarId
	val avatarURL get() = user.avatarUrl
	val bot get() = user.isBot
	// TODO: client
	val createdAt get() = user.timeCreated
	// TODO: creationTimestamp
	val defaultAvatarURL get() = user.defaultAvatarUrl
	val discriminator get() = user.discriminator
	val displayAvatarURL get() = user.effectiveAvatarUrl
	// TODO: dmChannel
	val id get() = user.id
	// TODO: lastMessage
	// TODO: presence
	val tag get() = user.name + "#" + user.discriminator
	val username get() = user.name

	// TODO: createDM
	// TODO: deleteDM

	override fun toString(): String {
		return "<@$id>"
	}
}