package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

class MutedUsersTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		logger.info("Verificando usuários temporariamente silenciados... Removal threads ativas: ${MuteCommand.roleRemovalJobs.size}")

		val mutes = transaction(Databases.loritta) {
			Mute.find {
				Mutes.isTemporary eq true
			}
		}

		for (mute in mutes) {
			val guild = lorittaShards.getGuildById(mute.guildId)
			if (guild == null) {
				logger.debug { "Guild \"${mute.guildId}\" não existe ou está indisponível!" }
				continue
			}

			val member = guild.getMemberById(mute.userId) ?: continue

			logger.info("Adicionado removal thread pelo MutedUsersThread ~ Guild: ${mute.guildId} - User: ${mute.userId}")
			MuteCommand.spawnRoleRemovalThread(guild, loritta.getLocaleById("default"), member.user, mute.expiresAt!!)
		}
	}
}