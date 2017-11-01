package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.save

class MutedUsersThread : Thread("Muted Users Thread") {
	override fun run() {
		while (true) {
			try {
				checkMuteStatus()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000)
		}
	}

	fun checkMuteStatus() {
		val guilds = loritta.ds.find(ServerConfig::class.java).field("userData").exists()

		for (guild in guilds) {
			val toBeUnmuted = guild.userData.filter { it.value.isMuted && it.value.temporaryMute && System.currentTimeMillis() > it.value.expiresIn }

			if (toBeUnmuted.isNotEmpty())
				continue

			val _guild = lorittaShards.getGuildById(guild.guildId)

			if (_guild == null)
				continue

			for ((key, userData) in toBeUnmuted) {
				try {
					userData.isMuted = false
					userData.temporaryMute = false

					val member = _guild.getMemberById(key)

					if (member == null)
						continue

					var mutedRoles = _guild.getRolesByName(loritta.getLocaleById(guild.localeId).MUTE_ROLE_NAME, false)

					if (mutedRoles.isEmpty())
						continue

					_guild.controller.addSingleRoleToMember(member, mutedRoles.first()).complete()
				} catch (e: Exception) {}
			}
			loritta save guild
		}
	}
}