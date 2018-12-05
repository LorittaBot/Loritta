package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Timers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TimersTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		val timerJobs = mutableMapOf<Long, Job>()
	}

	override fun run() {
		try {
			logger.info("Criando timers para servidores...")

			val timers = transaction(Databases.loritta) {
				Timers.selectAll().map { Timer.wrapRow(it) }
			}

			logger.info("Timers: ${timers.size}")

			for (timer in timers) {
				logger.info("Timer ${timer.id} em guild ${timer.guildId}")
				val guild = lorittaShards.getGuildById(timer.guildId) ?: continue
				logger.info("Timer ${timer.id} em canal ${timer.channelId}")
				val textChannel = guild.getTextChannelById(timer.channelId) ?: continue

				logger.info("Verificando jobs atuais de ${timer.id}...")

				if (timerJobs[timer.id.value] != null)
					return

				logger.info("Agora vai! Preparando timer ${timer.id}...")

				timerJobs[timer.id.value] = GlobalScope.launch(loritta.coroutineDispatcher) {
					timer.prepareTimer()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao verificar timer threads" }
		}
	}
}