package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import org.slf4j.LoggerFactory

class MutedUsersThread : Thread("Muted Users Thread") {
	companion object {
		val logger = LoggerFactory.getLogger(MutedUsersThread::class.java)
	}

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
		val servers = loritta.serversColl.find(
				Filters.eq("guildUserData.temporaryMute", true)
		)

		logger.info("Verificando usuários silenciados de ${servers.count()} servidores...")

		servers.iterator().use {
			while (it.hasNext()) {
				val next = it.next()
				val guild = lorittaShards.getGuildById(next.guildId) ?: continue
				val locale = loritta.getLocaleById(next.localeId)
				next.guildUserData.filter { it.temporaryMute }.forEach {
					if (!MuteCommand.roleRemovalThreads.containsKey("${guild.id}#${it.userId}")) {
						MuteCommand.spawnRoleRemovalThread(guild, locale, next, it)
					}
				}
			}
		}
	}
}