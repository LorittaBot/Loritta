package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.userdata.AutoroleConfig
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent

object AutoroleModule {
	fun giveRoles(event: GuildMemberJoinEvent, autoroleConfig: AutoroleConfig) {
		val rolesId = autoroleConfig.roles // Então vamos pegar todos os IDs...

		val roles = mutableListOf<Role>()

		rolesId.forEach { // E pegar a role dependendo do ID!
			try {
				val role = event.guild.getRoleById(it)

				if (role != null && !role.isPublicRole && !role.isManaged && event.guild.selfMember.canInteract(role)) {
					roles.add(role)
				}
			} catch (e: NumberFormatException) {} // The specified ID is not a valid snowflake (null).
		}

		if (roles.isNotEmpty()) {
			if (roles.size == 1) {
				event.guild.controller.addSingleRoleToMember(event.member, roles[0]).reason("Autorole").complete()
			} else {
				event.guild.controller.addRolesToMember(event.member, roles).reason("Autorole").complete()
			}
		}
	}
}