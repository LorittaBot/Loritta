package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Guild
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class TimersPayload : ConfigPayloadType("timers") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val timers = payload["timers"].array
		// Vamos primeiro remover TODOS os timers existentes da guild atual
		transaction(Databases.loritta) {
			Timers.deleteWhere { Timers.guildId eq guild.idLong }
		}
		
		// Certo? Agora vamos inserir os novos timers!
		transaction(Databases.loritta) {
			timers.forEach {
				Timer.new {
					// o ID do Timer tanto faz, sempre vai ser reinserido na tabela
					this.guildId = guild.idLong // Iremos sempre pegar o ID da guild do objeto, para evitar que alguém possa inserir um timer de outra guild
					this.channelId = it["channelId"].long
					this.startsAt = it["startsAt"].long
					this.repeatDelay = it["repeatDelay"].long
					this.effects = it["effects"].array.map { it.asString }.toTypedArray()
					this.activeOnDays = arrayOf()
				}
			}
		}
	}
}