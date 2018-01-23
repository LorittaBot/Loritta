package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.entities.Member

class ParallaxMember(private val member: Member) : ParallaxUser(member.user) {
	fun hasRole(role: ParallaxRole): Boolean {
		return member.roles.contains(role.role)
	}

	fun addRole(role: ParallaxRole): Boolean {
		return member.roles.add(role.role)
	}

	fun removeRole(role: ParallaxRole): Boolean {
		return member.roles.remove(role.role)
	}
}