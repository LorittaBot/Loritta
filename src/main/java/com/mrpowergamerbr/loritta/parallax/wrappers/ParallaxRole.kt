package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.entities.Role

class ParallaxRole(internal val role: Role) {
	val calculatedPosition get() = role.position
	val client = ParallaxClient(role.jda)
	val color get() = role.color.rgb
	// TODO: createdAt
	// TODO: createdTimestamp
	val editable get() = role.guild.selfMember.canInteract(role)
	val guild get() = ParallaxGuild(role.guild)
	// TODO: hexColor
	val hoist get() = role.isHoisted
	val id get() = role.id
	val managed get() = role.isManaged
	val members = role.guild.getMembersWithRoles(role).map { ParallaxMember(it) }
	val mentionable get() = role.isMentionable
	val name get() = role.name
	val permissions get() = role.permissionsRaw
	val position get() = role.positionRaw
}