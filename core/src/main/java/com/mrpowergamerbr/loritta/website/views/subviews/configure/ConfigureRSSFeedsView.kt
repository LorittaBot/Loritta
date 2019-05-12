package com.mrpowergamerbr.loritta.website.views.subviews.configure

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.website.evaluate
import net.dv8tion.jda.api.entities.Guild
import org.jooby.Request
import org.jooby.Response
import kotlin.collections.set

class ConfigureRSSFeedsView : ConfigureView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		super.handleRender(req, res, path, variables)
		return path.matches(Regex("^/dashboard/configure/[0-9]+/feeds"))
	}

	override fun renderConfiguration(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>, discordAuth: TemmieDiscordAuth, guild: Guild, serverConfig: MongoServerConfig): String {
		variables["saveType"] = "feeds"

		val feeds = JsonArray()
		serverConfig.rssFeedConfig.feeds.filter { it.repostToChannelId != null }.forEach {
			val json = Loritta.GSON.toJsonTree(it)
			val textChannel = guild.getTextChannelByNullableId(it.repostToChannelId)
			if (textChannel != null) {
				json["textChannelName"] = textChannel.name
				feeds.add(json)
			}
		}

		variables["feeds"] = feeds.toString()

		return evaluate("configure_rssfeeds.html", variables)
	}
}