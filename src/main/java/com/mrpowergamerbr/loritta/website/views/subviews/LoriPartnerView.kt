package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist.APIGetServerSampleView
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import org.jooby.Request
import org.jooby.Response
import java.io.File

class LoriPartnerView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		val arg0 = path.split("/").getOrNull(2) ?: return false

		val server = loritta.serversColl.find(
				Filters.or(
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("serverListConfig.vanityUrl", arg0)
						),
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("_id", arg0)
						)
				)
		).firstOrNull() ?: return false

		return path.startsWith("/s/")
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		val arg0 = path.split("/").getOrNull(2) ?: return ":whatdog:"
		variables["guildId"] = arg0
		val server = loritta.serversColl.find(
				Filters.or(
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("serverListConfig.vanityUrl", arg0)
						),
						Filters.and(
								Filters.eq("serverListConfig.enabled", true),
								Filters.eq("_id", arg0)
						)
				)
		).firstOrNull() ?: return "Something went wrong, sorry."

		val guild = lorittaShards.getGuildById(server.guildId) ?: return "Something went wrong, sorry."

		variables["serverListConfig"] = server.serverListConfig
		variables["guild"] = guild
		var tagline = server.serverListConfig.tagline ?: ""
		guild.emotes.forEach {
			tagline = tagline.replace(":${it.name}:", "")
		}
		variables["tagline"] = tagline
		variables["iconUrl"] = guild.iconUrl?.replace("jpg", "png?size=512")
		variables["hasCustomBackground"] = File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${server.guildId}.png").exists()

		var userIdentification: TemmieDiscordAuth.UserIdentification? = null

		if (!req.session().isSet("discordAuth")) {
			variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
		} else {
			try {
				val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
				val profile = loritta.getLorittaProfileForUser(userIdentification.id)

				variables["selfProfile"] = Loritta.GSON.toJson(profile)
			} catch (e: Exception) {
				variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
			}
		}

		val information = APIGetServerSampleView.transformToJsonObject(guild, server, userIdentification)
		variables["serverInformation"] = GSON.toJson(information)

		return evaluate("partner_view.html", variables)
	}
}