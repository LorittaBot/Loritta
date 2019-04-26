package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.TimersTask
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class TimersPayload : ConfigPayloadType("timers") {
	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		val timers = payload["timers"].array

		// Vamos primeiro remover TODOS os timers existentes da guild atual
		transaction(Databases.loritta) {
			val currentTimers = Timer.find { Timers.guildId eq guild.idLong }

			currentTimers.forEach { // Vamos cancelar TODOS os Timers atuais da guild
				TimersTask.timerJobs[it.id.value]?.cancel()
				TimersTask.timerJobs.remove(it.id.value)
			}

			Timers.deleteWhere { Timers.guildId eq guild.idLong }
		}

		// Certo? Agora vamos inserir os novos timers!
		transaction(Databases.loritta) {
			timers.forEach {
				val timer = Timer.new {
					// o ID do Timer tanto faz, sempre vai ser reinserido na tabela
					this.guildId = guild.idLong // Iremos sempre pegar o ID da guild do objeto, para evitar que alguém possa inserir um timer de outra guild
					this.channelId = it["channelId"].long
					this.startsAt = it["startsAt"].long
					this.repeatDelay = it["repeatDelay"].long
					this.effects = it["effects"].array.map { it.asString }.toTypedArray()
					this.activeOnDays = arrayOf()
				}

				TimersTask.timerJobs[timer.id.value] = GlobalScope.launch(loritta.coroutineDispatcher) {
					timer.prepareTimer()
				}
			}
		}
	}
}