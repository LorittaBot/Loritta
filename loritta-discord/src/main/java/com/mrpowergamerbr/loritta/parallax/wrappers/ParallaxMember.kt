package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.api.entities.Member

class ParallaxMember(private val member: Member) : ParallaxUser(member.user) {
	fun hasRole(role: ParallaxRole): Boolean {
		return member.roles.contains(role.role)
	}

	fun addRole(role: ParallaxRole): Boolean {
		member.guild.addRoleToMember(member, role.role).queue()
		return true
	}

	fun removeRole(role: ParallaxRole): Boolean {
		member.guild.removeRoleFromMember(member, role.role).queue()
		return true
	}

	fun canInteract(role: ParallaxRole): Boolean {
		return member.canInteract(role.role)
	}

	fun canInteract(member: ParallaxMember): Boolean {
		return this.member.canInteract(member.member)
	}
}