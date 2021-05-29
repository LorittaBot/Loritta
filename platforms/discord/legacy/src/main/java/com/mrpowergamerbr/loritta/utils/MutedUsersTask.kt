package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNullById
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

class MutedUsersTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			logger.info("Verificando usuários temporariamente silenciados... Removal threads ativas: ${MuteCommand.roleRemovalJobs.size}")

			val mutes = transaction(Databases.loritta) {
				Mute.find {
					Mutes.isTemporary eq true
				}.toMutableList()
			}

			val guildLocales = mutableMapOf<Long, String>()

			for (mute in mutes) {
				val expiresAt = mute.expiresAt

				if (expiresAt != null && expiresAt >= System.currentTimeMillis() + Constants.DELAY_CUT_OFF) { // Não crie giveaways caso o tempo seja alto demais
					logger.debug { "Not creating mute task ${mute.id.value}, it will expire at ${mute.expiresAt} and that's waaay too damn long!" }
					continue
				}

				val guild = lorittaShards.getGuildById(mute.guildId)

				if (guild == null) {
					logger.debug { "Guild \"${mute.guildId}\" não existe ou está indisponível!" }

					deleteMuteIfNeeded(mute)
					continue
				}

				val member = runBlocking { guild.retrieveMemberOrNullById(mute.userId) }

				if (member == null) {
					logger.debug { "Member ${mute.userId} has a mute status in $guild, but the member isn't there anymore!" }

					deleteMuteIfNeeded(mute)
					deleteMuteIfNeededMemberIsNotOnTheServer(mute)
					continue
				}

				val jobId = "${guild.idLong}#${member.idLong}"

				val previousJob = MuteCommand.roleRemovalJobs[jobId]

				if (previousJob != null) // Se já tem um job criado, não recrie (é desnecessário!)
					continue

				logger.info { "Adicionado removal thread pelo MutedUsersThread ~ Guild: ${mute.guildId} - User: ${mute.userId}" }

				val localeId = guildLocales.getOrPut(mute.guildId, { loritta.getOrCreateServerConfig(mute.guildId).localeId })
				MuteCommand.spawnRoleRemovalThread(guild, loritta.localeManager.getLocaleById(localeId), member.user, mute.expiresAt!!)
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao verificar removal threads" }
		}
	}

	/**
	 * Deletes the [mute] if the mute was meant to be expired [Constants.ONE_WEEK_IN_MILLISECONDS] ago, but wasn't deleted (guild does not exists anymore, etc)
	 *
	 * @return if the mute was deleted
	 */
	fun deleteMuteIfNeeded(mute: Mute): Boolean {
		val expiresAt = mute.expiresAt

		if (expiresAt != null && System.currentTimeMillis() - Constants.ONE_WEEK_IN_MILLISECONDS >= expiresAt) { // Já se passaram uma semana?
			logger.debug { "Deleting $mute from database... The mute was made more than one week ago and it wasn't deleted..." }
			transaction(Databases.loritta) {
				mute.delete()
			}
			return true
		}
		return false
	}

	/**
	 * Deletes the [mute] if the muted member isn't on the guild, and mute was created [Constants.SIX_MONTHS_IN_MILLISECONDS] ago
	 *
	 * @return if the mute was deleted
	 */
	fun deleteMuteIfNeededMemberIsNotOnTheServer(mute: Mute): Boolean {
		val receivedAt = mute.receivedAt

		if (System.currentTimeMillis() - Constants.SIX_MONTHS_IN_MILLISECONDS >= receivedAt) { // Já se passaram uma semana?
			logger.debug { "Deleting $mute from database... Member is not on the server anymore and the mute was done a long time ago..." }
			transaction(Databases.loritta) {
				mute.delete()
			}
			return true
		}
		return false
	}
}