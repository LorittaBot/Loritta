package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.userdata.AutoroleConfig
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import java.util.concurrent.TimeUnit

object AutoroleModule {
	fun giveRoles(member: Member, autoroleConfig: AutoroleConfig) {
		val guild = member.guild
		val rolesId = autoroleConfig.roles // Então vamos pegar todos os IDs...

		val roles = mutableListOf<Role>()

		rolesId.forEach { // E pegar a role dependendo do ID!
			try {
				val role = guild.getRoleById(it)

				if (role != null && !role.isPublicRole && !role.isManaged && guild.selfMember.canInteract(role)) {
					roles.add(role)
				}
			} catch (e: NumberFormatException) {} // The specified ID is not a valid snowflake (null).
		}

		val filteredRoles = roles.filter { !member.roles.contains(it) }

		if (filteredRoles.isNotEmpty()) {
			if (filteredRoles.size == 1) {
				if (autoroleConfig.giveRolesAfter != null)
					guild.addRoleToMember(member, filteredRoles[0]).reason("Autorole").queueAfter(autoroleConfig.giveRolesAfter!!, TimeUnit.SECONDS)
				else
					guild.addRoleToMember(member, filteredRoles[0]).reason("Autorole").queue()
			} else {
				if (autoroleConfig.giveRolesAfter != null)
					guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(filteredRoles) }).reason("Autorole").queueAfter(autoroleConfig.giveRolesAfter!!, TimeUnit.SECONDS)
				else
					guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(filteredRoles) }).reason("Autorole").queue()
			}
		}
	}
}