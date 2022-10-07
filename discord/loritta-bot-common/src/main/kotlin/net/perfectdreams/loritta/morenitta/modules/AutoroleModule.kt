package net.perfectdreams.loritta.morenitta.modules

import kotlinx.coroutines.delay
import net.perfectdreams.loritta.morenitta.utils.extensions.filterOnlyGiveableRoles
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.queue
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

object AutoroleModule {
	suspend fun giveRoles(member: Member, autoroleConfig: AutoroleConfig) {
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
				if (autoroleConfig.giveRolesAfter != null) {
					delay(autoroleConfig.giveRolesAfter!!.seconds)

					guild.addRoleToMember(member, filteredRoles[0], "Autorole")
				} else
					guild.addRoleToMember(member, filteredRoles[0], "Autorole").queue()
			} else {
				if (autoroleConfig.giveRolesAfter != null) {
					delay(autoroleConfig.giveRolesAfter!!.seconds)

					guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(filteredRoles) }, "Autorole")
				} else
					guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(filteredRoles) }, "Autorole").queue()
			}
		}
	}
}