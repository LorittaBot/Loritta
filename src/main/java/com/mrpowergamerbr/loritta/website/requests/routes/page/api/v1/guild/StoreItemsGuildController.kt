package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/guild/:guildId/store-items")
class StoreItemsGuildController {
	val logger by logger()

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun buyItem(req: Request, res: Response, @Body rawItemPayload: String) {
		val itemPayload = jsonParser.parse(rawItemPayload)

		val uniqueId = itemPayload["uuid"].string

		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) { // Unauthorized (Discord)
			res.status(Status.UNAUTHORIZED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNAUTHORIZED,
							"Invalid Discord Authorization"
					)
			)
			return
		}

		// TODO: Permitir customizar da onde veio o guildId
		val guildId = req.path().split("/")[4]

		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			res.status(Status.BAD_REQUEST)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNKNOWN_GUILD,
							"Guild $guildId doesn't exist or it isn't loaded yet"
					)
			)
			return
		}

		val id = userIdentification.id
		if (id != Loritta.config.ownerId) {
			val member = server.getMemberById(id)

			if (member == null) {
				res.status(Status.BAD_REQUEST)
				res.send(
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MEMBER_NOT_IN_GUILD,
								"Member $id is not in guild ${server.id}"
						)
				)
				return
			}
		}

		val storeItem = serverConfig.economyConfig.storeItems.firstOrNull { it.uniqueId.toString() == uniqueId } ?: run {
			res.status(Status.NOT_FOUND)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND,
							"Item $uniqueId doesn't exist"
					)
			)
			return
		}

		res.send("${storeItem.uniqueId}")
	}
}