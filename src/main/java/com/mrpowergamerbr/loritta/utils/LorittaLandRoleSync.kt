package com.mrpowergamerbr.loritta.utils

import net.dv8tion.jda.core.entities.Guild

class LorittaLandRoleSync : Runnable {
	override fun run() {
		val roleRemap = mutableMapOf(
				"316363779518627842" to "420630427837923328", // Deusas Supremas
				"301764115582681088" to "420630186061725696", // Loritta (Integration)
				"351473717194522647" to "421325022951637015", // Guarda-Costas da Lori
				"334734175531696128" to "420710241693466627" // Notificar Novidades
		)

		val originalGuild = lorittaShards.getGuildById("297732013006389252") ?: return
		val usGuild = lorittaShards.getGuildById("420626099257475072") ?: return

		for ((originalRoleId, usRoleId) in roleRemap) {
			val originalRole = originalGuild.getRoleById(originalRoleId)
			val usRole = usGuild.getRoleById(originalRoleId)

			val manager = usRole.manager
			var changed = false

			if (originalRole.color != usRole.color) {
				manager.setColor(originalRole.color)
				changed = true
			}

			if (originalRole.permissionsRaw != usRole.permissionsRaw) {
				manager.setPermissions(usRole.permissionsRaw)
				changed = true
			}

			if (changed)
				manager.complete()
		}

		// Give roles
		giveRoles(originalGuild, usGuild, "351473717194522647", "421325022951637015")
	}

	fun giveRoles(fromGuild: Guild, toGuild: Guild, originalRoleId: String, giveRoleId: String) {
		val originalRole = fromGuild.getRoleById(originalRoleId)
		val giveRole = fromGuild.getRoleById(giveRoleId)

		val membersWithOriginalRole = fromGuild.getMembersWithRoles(originalRole)
		val membersWithNewRole = toGuild.getMembersWithRoles(giveRole)

		for (member in membersWithNewRole) {
			if (!membersWithOriginalRole.any { it.user == member.user }) {
				toGuild.controller.removeSingleRoleFromMember(member, giveRole).complete()
			}
		}

		for (member in membersWithOriginalRole) {
			if (!membersWithNewRole.any { it.user == member.user }) {
				toGuild.controller.addSingleRoleToMember(member, giveRole).complete()
			}
		}
	}
}