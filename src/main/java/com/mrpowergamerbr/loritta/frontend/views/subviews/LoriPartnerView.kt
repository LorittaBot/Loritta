package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.OnlineStatus
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import java.io.File
import org.jsoup.safety.Whitelist

class LoriPartnerView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		val arg0 = req.path().split("/").getOrNull(2) ?: return false

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

		return req.path().startsWith("/s/")
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val arg0 = req.path().split("/").getOrNull(2) ?: return ":whatdog:"
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
		return evaluate("partner_view.html", variables)
	}
}