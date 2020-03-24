package net.perfectdreams.loritta.plugin.lorittabirthday2020.routes

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.gson
import io.ktor.application.ApplicationCall
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.response.cacheControl
import io.ktor.response.respondTextWriter
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020Event
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ReceiveStatsRoute(val m: LorittaBirthday2020Event, loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/birthday-2020/stats") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val response = call.response
		response.cacheControl(CacheControl.NoCache(null))

		val idLong = userIdentification.id.toLong()

		// Caso já tenha um channel aberto, feche ele
		logger.info { "Checking open channels" }
		val currentOpenChannel = LorittaBirthday2020.openChannels[idLong]
		logger.info { "Closing ${currentOpenChannel}" }
		currentOpenChannel?.close()

		logger.info { "Creating a new channel" }
		val channel = Channel<JsonObject>()
		logger.info { "Storing the new channel" }
		LorittaBirthday2020.openChannels[idLong] = channel
		call.respondTextWriter(contentType = ContentType.Text.EventStream) {
			// Enviar qualquer coisa só para deixar aberto
			logger.info { "Synchronizing points on initial connection" }
			LorittaBirthday2020.sendPresentCount(m, idLong, "syncPoints")

			try {
				logger.info { "waitin' and sending" }
				for (payload in channel) {
					logger.info { "sending $payload" }
					write("data: ${gson.toJson(payload)}\n")
					write("\n")
					flush()
				}
			} catch (e: Throwable) {
				logger.warn(e) { "omg" }
				LorittaBirthday2020.openChannels.remove(idLong, channel)
				channel.close()
			}
		}
	}
}