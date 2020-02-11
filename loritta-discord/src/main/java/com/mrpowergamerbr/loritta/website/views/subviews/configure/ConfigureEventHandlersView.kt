package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response

class ConfigureEventHandlersView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/eventhandlers"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "event_handlers"

		val feeds = JsonArray()
		serverConfig.nashornEventHandlers.forEach {
			val json = Loritta.GSON.toJsonTree(it)
			feeds.add(json)
		}

		variables["eventHandlers"] = feeds.toString()

		return evaluate("configure_eventhandlers.html", variables)
	}
}