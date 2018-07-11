package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist.APIGetServerSampleView
import org.jooby.Request
import org.jooby.Response

class ServersView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		return path.startsWith("/servers")
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		// se path == /serversfanclub, aplicar redirect
		if (path.equals("/serversfanclub")) {
			res.status(301) // permanent redirect
			res.redirect("https://loritta.website/servers")
			return "Location: https://loritta.website/servers"
		}

		val args = path.split("/")
		val arg2 = args.getOrNull(2)

		if (arg2 == "faq") {
			return evaluate("sponsored_faq.html", variables)
		}

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

		variables["serverSamples"] = Loritta.GSON.toJson(getSamples(userIdentification))

		return evaluate("server_list.html", variables)
	}

	fun getSamples(userIdentification: TemmieDiscordAuth.UserIdentification?): JsonObject {
		val sponsoredConfigs = loritta.serversColl
				.aggregate(
						listOf(
								Aggregates.match(
										Filters.or(
												Filters.and(
														Filters.eq("serverListConfig.enabled", true),
														Filters.eq("serverListConfig.sponsored", true),
														Filters.gt("serverListConfig.sponsoredUntil", System.currentTimeMillis())),
												Filters.and(
														Filters.eq("serverListConfig.enabled", true),
														Filters.eq("serverListConfig.sponsored", true),
														Filters.eq("serverListConfig.sponsoredUntil", -1))
										)
								),
								Aggregates.sort(Sorts.descending("serverListConfig.sponsorPaid"))
						)
				)

		val partnerConfigs = loritta.serversColl
				.aggregate(
						listOf(
								Aggregates.match(Filters.and(
										Filters.eq("serverListConfig.enabled", true),
										Filters.eq("serverListConfig.partner", true)
								)
								),
								Aggregates.sample(8)
						)
				)

		val query = org.bson.Document.parse("{ \$addFields: { \"serverListConfig.validVotes\": { \$filter: { input: \"\$serverListConfig.votes\", as: \"item\", cond: {\$gt: [\"\$\$item.votedAt\", ${System.currentTimeMillis() - 2592000000}]}}}}}")
		val topConfigs = loritta.serversColl
				.aggregate(
						listOf(
								Aggregates.match(Filters.eq("serverListConfig.enabled", true)),
								query,
								org.bson.Document("\$addFields", org.bson.Document("length", org.bson.Document("\$size", org.bson.Document("\$ifNull", listOf("\$serverListConfig.validVotes", emptyList<Any>()))))),
								Aggregates.sort(Sorts.descending("length")),
								Aggregates.limit(25))
				)

		val recentlyBumped = loritta.serversColl
				.aggregate(
						listOf(
								Aggregates.match(Filters.eq("serverListConfig.enabled", true)),
								query,
								org.bson.Document("\$addFields", org.bson.Document("length", org.bson.Document("\$size", org.bson.Document("\$ifNull", listOf("\$serverListConfig.validVotes", emptyList<Any>()))))),
								Aggregates.sort(Sorts.descending("serverListConfig.lastBump")),
								Aggregates.limit(25))
				)

		val randomConfigs =
				loritta.serversColl
						.aggregate(
								listOf(
										Aggregates.match(Filters.eq("serverListConfig.enabled", true)),
										Aggregates.sample(26)
								)
						)

		val sponsoredArray = APIGetServerSampleView.transformToJsonArray(sponsoredConfigs.toMutableList(), userIdentification)
		val partnerArray = APIGetServerSampleView.transformToJsonArray(partnerConfigs.toMutableList(), userIdentification)
		val topArray = APIGetServerSampleView.transformToJsonArray(topConfigs.toMutableList(), userIdentification)
		val randomArray = APIGetServerSampleView.transformToJsonArray(randomConfigs.toMutableList(), userIdentification)
		val recentlyArray = APIGetServerSampleView.transformToJsonArray(recentlyBumped.toMutableList(), userIdentification)

		val samples = JsonObject()
		samples["sponsored"] = sponsoredArray
		samples["partners"] = partnerArray
		samples["top"] = topArray
		samples["recentlyBumped"] = recentlyArray // os bumped recente sempre será totalCount
		samples["random"] = randomArray
		samples["sponsoredCount"] = loritta.serversColl.count(Filters.or(
				Filters.and(
						Filters.eq("serverListConfig.enabled", true),
						Filters.eq("serverListConfig.sponsored", true),
						Filters.gt("serverListConfig.sponsoredUntil", System.currentTimeMillis())),
				Filters.and(
						Filters.eq("serverListConfig.enabled", true),
						Filters.eq("serverListConfig.sponsored", true),
						Filters.eq("serverListConfig.sponsoredUntil", -1))
		))
		samples["partnersCount"] = loritta.serversColl.count(Filters.and(
				Filters.eq("serverListConfig.enabled", true),
				Filters.eq("serverListConfig.partner", true)
		))

		samples["totalCount"] = loritta.serversColl.count(
				Filters.eq("serverListConfig.enabled", true)
		)

		return samples
	}
}