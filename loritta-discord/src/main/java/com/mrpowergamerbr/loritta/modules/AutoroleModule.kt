package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.utils.extensions.filterOnlyGiveableRoles
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.dao.servers.moduleconfigs.AutoroleConfig
import java.util.concurrent.TimeUnit

object AutoroleModule {
	fun giveRoles(member: Member, autoroleConfig: AutoroleConfig) {
		val guild = member.guild
		// Transform all role IDs to a role list
		val roles = autoroleConfig.roles
				.asSequence()
				.mapNotNull { guild.getRoleById(it) }
				.distinct()
				.filterOnlyGiveableRoles()
				.toList()

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