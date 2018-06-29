package com.mrpowergamerbr.loritta.website.views.subviews.api.serverlist

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.views.subviews.api.NoVarsView
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.io.File
import java.util.*

@Deprecated(message = "Now embed within the page")
class APIGetServerSampleView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/server-list/get-sample"))
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

		val sponsoredArray = transformToJsonArray(sponsoredConfigs.toMutableList(), userIdentification)
		val partnerArray = transformToJsonArray(partnerConfigs.toMutableList(), userIdentification)
		val topArray = transformToJsonArray(topConfigs.toMutableList(), userIdentification)
		val randomArray = transformToJsonArray(randomConfigs.toMutableList(), userIdentification)
		val recentlyArray = transformToJsonArray(recentlyBumped.toMutableList(), userIdentification)

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

		return samples.toString()
	}

	companion object {
		fun transformToJsonArray(serverConfigs: List<ServerConfig>, userIdentification: TemmieDiscordAuth.UserIdentification?): JsonArray {
			val allServersSample = JsonArray()
			for (server in serverConfigs.toList()) {
				val guild = lorittaShards.getGuildById(server.guildId) ?: continue

				if (guild.owner == null) // Alguns servidores, por algum motivo, não possuem dono (como?)
					continue // Por isto nós iremos ignorar tais servidores

				allServersSample.add(transformToJsonObject(guild, server, userIdentification))
			}
			return allServersSample
		}

		fun transformToJsonObject(guild: Guild, server: ServerConfig, userIdentification: TemmieDiscordAuth.UserIdentification?): JsonObject {
			val information = JsonObject()
			server.serverListConfig.description = Jsoup.clean(server.serverListConfig.description, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
			server.serverListConfig.tagline = Jsoup.clean(server.serverListConfig.tagline, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
			information["serverListConfig"] = Gson().toJsonTree(server.serverListConfig)
			information["serverListConfig"].obj.remove("votes")
			information["id"] = guild.id
			information["iconUrl"] = guild.iconUrl?.replace("jpg", "png")
			information["name"] = guild.name
			information["tagline"] = server.serverListConfig.tagline
			information["description"] = server.serverListConfig.description
			information["keywords"] = Loritta.GSON.toJsonTree(server.serverListConfig.keywords)
			information["ownerId"] = guild.owner.user.id
			information["ownerName"] = guild.owner.user.name
			information["ownerDiscriminator"] = guild.owner.user.discriminator
			information["ownerAvatarUrl"] = guild.owner.user.effectiveAvatarUrl.replace("jpg", "png")
			information["memberCount"] = guild.members.size
			information["onlineCount"] = guild.members.count { it.onlineStatus != OnlineStatus.OFFLINE }
			information["serverCreatedAt"] = guild.creationTime.toEpochSecond() * 1000
			information["joinedAt"] = guild.selfMember.joinDate.toEpochSecond() * 1000
			val background = File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${server.guildId}.png")
			information["hasCustomBackground"] = background.exists()
			if (background.exists()) {
				// Para evitar que o usuário tenha que baixar o background toda hora que a página é recarregada, nós iremos salvar uma key "única" para cada bg
				// Para ficar mais fácil, nós iremos usar a data de modificação do arquivo como o "background key", já que toda hora que o usuário troca o
				// background, a data de modificação irá mudar também.
				information["backgroundKey"] = background.lastModified()
			}

			information["voteCount"] = server.serverListConfig.votes.size
			information["validVoteCount"] = server.serverListConfig.votes.count { it.votedAt > System.currentTimeMillis() - 2592000000}
			information["canVote"] = true
			information["lastBump"] = server.serverListConfig.lastBump
			// 1 = not logged in
			// 2 = not member
			// 3 = needs to wait more than 1 hour before voting
			// 4 = needs to wait until next day
			if (userIdentification != null) {
				val isMember = guild.getMemberById(userIdentification.id) != null

				if (!isMember) {
					information["canVote"] = false
					information["cantVoteReason"] = 2
				} else {
					val vote = server.serverListConfig.votes
							.lastOrNull { it.id == userIdentification.id }

					if (vote == null) {
						information["canVote"] = true
					} else {
						val votedAt = vote.votedAt

						val calendar = Calendar.getInstance()
						calendar.timeInMillis = votedAt
						calendar.set(Calendar.HOUR_OF_DAY, 0)
						calendar.set(Calendar.MINUTE, 0)
						calendar.add(Calendar.DAY_OF_MONTH, 1)
						val tomorrow = calendar.timeInMillis

						val canVote = System.currentTimeMillis() > tomorrow

						if (canVote) {
							information["canVote"] = true
						} else {
							information["canVote"] = false
							information["cantVoteReason"] = 4
							information["canVoteNext"] = tomorrow
						}
					}
				}
			} else {
				information["canVote"] = false
				information["cantVoteReason"] = 1
			}

			val serverEmotes = JsonArray()

			guild.emotes.forEach {
				val emote = JsonObject()
				emote["name"] = it.name
				emote["imageUrl"] = it.imageUrl
				serverEmotes.add(emote)
			}

			information["serverEmotes"] = serverEmotes
			information["joinedServer"] = if (userIdentification != null) { guild.getMemberById(userIdentification.id) != null } else { false }
			return information
		}
	}
}