package com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response

class APIGetServersView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/get-servers"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
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

		val skip = req.param("skip").intValue(0)
		val size = Math.min(req.param("size").intValue(49), 99)
		val serverType = req.param("serverType").value()

		val query = org.bson.Document.parse("{ \$addFields: { \"serverListConfig.validVotes\": { \$filter: { input: \"\$serverListConfig.votes\", as: \"item\", cond: {\$gt: [\"\$\$item.votedAt\", ${System.currentTimeMillis() - 2592000000}]}}}}}")
		if (serverType == "top") {
			val topConfigs = loritta.serversColl
					.aggregate(
							listOf(
									Aggregates.match(Filters.eq("serverListConfig.enabled", true)),
									query,
									org.bson.Document("\$addFields", org.bson.Document("length", org.bson.Document("\$size", org.bson.Document("\$ifNull", listOf("\$serverListConfig.validVotes", emptyList<Any>()))))),
									Aggregates.sort(Sorts.descending("length")),
									Aggregates.skip(skip),
									Aggregates.limit(size)
							)
					)


			val topArray = APIGetServerSampleView.transformToJsonArray(topConfigs.toMutableList(), userIdentification)

			val samples = JsonObject()

			samples["result"] = topArray
			samples["totalCount"] = loritta.serversColl.count(
					Filters.eq("serverListConfig.enabled", true)
			)

			return samples.toString()
		} else if (serverType == "recentlyBumped") {
			val topConfigs = loritta.serversColl
					.aggregate(
							listOf(
									Aggregates.match(Filters.eq("serverListConfig.enabled", true)),
									query,
									org.bson.Document("\$addFields", org.bson.Document("length", org.bson.Document("\$size", org.bson.Document("\$ifNull", listOf("\$serverListConfig.validVotes", emptyList<Any>()))))),
									Aggregates.sort(Sorts.descending("serverListConfig.lastBump")),
									Aggregates.skip(skip),
									Aggregates.limit(size)
							)
					)


			val topArray = APIGetServerSampleView.transformToJsonArray(topConfigs.toMutableList(), userIdentification)

			val samples = JsonObject()

			samples["result"] = topArray
			samples["totalCount"] = loritta.serversColl.count(
					Filters.eq("serverListConfig.enabled", true)
			)

			return samples.toString()
		}
		return JsonObject().toString()
	}
}