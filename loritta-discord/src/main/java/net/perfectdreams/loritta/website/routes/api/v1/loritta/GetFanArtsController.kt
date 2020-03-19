package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetFanArtsController(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/fan-arts") {
	fun filterEntries(node: ObjectNode, vararg fields: String): JsonNode {
		val toBeRemoved = mutableSetOf<String>()

		node.fieldNames().forEach {
			if (!fields.contains(it))
				toBeRemoved.add(it)
		}

		toBeRemoved.forEach { node.remove(it) }

		return node
	}

	override suspend fun onRequest(call: ApplicationCall) {
		val query = call.parameters["query"]
		val filter = call.parameters["filter"]?.split(",")

		val fanArtArtists = com.mrpowergamerbr.loritta.utils.loritta.fanArtArtists
				.let {
					if (filter != null)
						it.filter { it.id in filter }
					else
						it
				}

		val fanArtists = Constants.JSON_MAPPER.valueToTree<JsonNode>(fanArtArtists)

		if (query == "all") {
			val discordIds = fanArtArtists
					.mapNotNull {
						it.socialNetworks?.asSequence()?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
								?.firstOrNull()?.let { discordInfo ->
									discordInfo.id.toLong()
								}
					}

			val users = discordIds.asSequence().mapNotNull { com.mrpowergamerbr.loritta.utils.loritta.cachedRetrievedArtists.getIfPresent(it)?.getOrNull() }

			fanArtists.map {
				val discordInfo = it["networks"]?.firstOrNull { it["type"].textValue() == "discord" }

				if (discordInfo != null) {
					val id = discordInfo["id"].textValue()
					val user = users.firstOrNull { it.id.toString() == id }

					if (user != null) {
						it as ObjectNode
						it.set<JsonNode>("user", objectNode(
								"id" to user.id,
								"name" to user.name,
								"effectiveAvatarUrl" to user.effectiveAvatarUrl
						))
					}
				}
			}
		}

		call.respondJson(fanArtists)
	}
}