package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.extensions.obj
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/fan-arts")
class GetFanArtsController {
	fun filterEntries(node: ObjectNode, vararg fields: String): JsonNode {
		val toBeRemoved = mutableSetOf<String>()

		node.fieldNames().forEach {
			if (!fields.contains(it))
				toBeRemoved.add(it)
		}

		toBeRemoved.forEach { node.remove(it) }

		return node
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)
		res.status(Status.OK)

		val fanArtists = Constants.JSON_MAPPER.valueToTree<JsonNode>(loritta.fanArtArtists)

		if (req.param("query").valueOrNull() == "all") {
			val discordIds = loritta.fanArtArtists
					.mapNotNull {
						it.socialNetworks?.asSequence()?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
								?.firstOrNull()?.let { discordInfo ->
									discordInfo.id.toLong()
								}
					}

			val users = discordIds.asSequence().mapNotNull { lorittaShards.getUserById(it) }
					.map { JDAUser(it) }.toList()

			fanArtists.map {
				val discordInfo = it["networks"]?.firstOrNull { it["type"].textValue() == "discord" }

				if (discordInfo != null) {
					val id = discordInfo["id"].textValue()
					val user = users.firstOrNull { it.idAsString == id }

					if (user != null) {
						it.obj["user"] = filterEntries(Constants.JSON_MAPPER.valueToTree(user), "id", "name", "effectiveAvatarUrl")
					}
				}
			}
		}

		res.send(
				Constants.JSON_MAPPER.writeValueAsString(fanArtists)
		)
	}
}