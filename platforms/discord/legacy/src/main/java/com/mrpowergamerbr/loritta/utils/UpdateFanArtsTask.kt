package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.utils.config.FanArtArtist
import java.util.*

class UpdateFanArtsTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		if (!loritta.isMaster) {
			try {
				val content = runBlocking { lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/fan-arts").await() }
				loritta.fanArtArtists = Constants.JSON_MAPPER.readValue(gson.toJson(content)) // Gambiarra para converter de Gson para Jackson
			} catch (e: Exception) {
				logger.warn(e) { "Error while trying to update fan arts from master cluster" }
			}
		} else {
			val discordIds = loritta.fanArtArtists
					.mapNotNull {
						it.socialNetworks?.asSequence()?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
								?.firstOrNull()?.let { discordInfo ->
									discordInfo.id.toLong()
								}
					}.filter {
						loritta.cachedRetrievedArtists.getIfPresent(it) == null
					}

			// Mesmo que a gente seja o cluster principal, existe algumas coisinhas que nós devemos fazer para vários requests ao acessar a página de fan arts
			// Nós iremos manter um cache de users de artistas, assim evitando vários requests ao carregar a página de fan arts.
			for (id in discordIds) {
				val user = runBlocking { lorittaShards.retrieveUserInfoById(id) }
				loritta.cachedRetrievedArtists.put(id, Optional.ofNullable(user))
			}
		}
	}
}