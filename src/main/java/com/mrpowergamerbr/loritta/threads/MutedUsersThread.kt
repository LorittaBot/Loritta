package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging

class MutedUsersThread : Thread("Muted Users Thread") {
	companion object {
		private val logger = KotlinLogging.logger {}
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

		logger.info("Verificando usuários temporariamente silenciados de ${servers.count()} servidores... Removal threads ativas: ${MuteCommand.roleRemovalThreads.size}")

		servers.iterator().use {
			while (it.hasNext()) {
				val next = it.next()
				val guild = lorittaShards.getGuildById(next.guildId)

				if (guild == null) {
					logger.debug { "Guild \"${next.guildId}\" não existe ou está indisponível!" }
					continue
				}

				val locale = loritta.getLocaleById(next.localeId)
				next.guildUserData.filter { it.temporaryMute }.forEach {
					if (!MuteCommand.roleRemovalThreads.containsKey("${guild.id}#${it.userId}") && guild.getMemberById(it.userId) != null) {
						logger.info("Adicionado removal thread pelo MutedUsersThread ~ Guild: ${next.guildId} - User: ${it.userId}")
						MuteCommand.spawnRoleRemovalThread(guild, locale, next, it)
					}
				}
			}
		}
	}
}