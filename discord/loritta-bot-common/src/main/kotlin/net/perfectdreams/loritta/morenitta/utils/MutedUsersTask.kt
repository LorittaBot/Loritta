package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand
import net.perfectdreams.loritta.morenitta.dao.Mute
import net.perfectdreams.loritta.morenitta.tables.Mutes
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot

class MutedUsersTask(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		try {
			logger.info("Verificando usuários temporariamente silenciados... Removal threads ativas: ${MuteCommand.roleRemovalJobs.size}")

			val mutes = runBlocking {
				loritta.pudding.transaction {
					Mute.find {
						Mutes.isTemporary eq true
					}.toMutableList()
				}
			}

			val guildLocales = mutableMapOf<Long, String>()

			for (mute in mutes) {
				val expiresAt = mute.expiresAt

				if (expiresAt != null && expiresAt >= System.currentTimeMillis() + Constants.DELAY_CUT_OFF) { // Não crie giveaways caso o tempo seja alto demais
					logger.debug { "Not creating mute task ${mute.id.value}, it will expire at ${mute.expiresAt} and that's waaay too damn long!" }
					continue
				}

				val guild = loritta.lorittaShards.getGuildById(mute.guildId)

				if (guild == null) {
					logger.debug { "Guild \"${mute.guildId}\" não existe ou está indisponível!" }

					deleteMuteIfNeeded(mute)
					continue
				}

				val jobId = "${guild.idLong}#${mute.userId}"

				val previousJob = MuteCommand.roleRemovalJobs[jobId]

				if (previousJob != null) // Se já tem um job criado, não recrie (é desnecessário!)
					continue

				logger.info { "Adicionado removal thread pelo MutedUsersThread ~ Guild: ${mute.guildId} - User: ${mute.userId}" }

				val localeId = guildLocales.getOrPut(mute.guildId) { loritta.getOrCreateServerConfig(mute.guildId).localeId }
				MuteCommand.spawnRoleRemovalThread(loritta, guild.idLong, loritta.localeManager.getLocaleById(localeId), mute.userId, mute.expiresAt!!)
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
			runBlocking {
				loritta.pudding.transaction {
					mute.delete()
				}
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
			runBlocking {
				loritta.pudding.transaction {
					mute.delete()
				}
			}
			return true
		}
		return false
	}
}