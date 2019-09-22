package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.commands.vanilla.misc.PingCommand
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.utils.DiscordUtils
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.io.File
import java.util.*

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

		val cluster = DiscordUtils.getLorittaClusterForShardId(server.guildId.toLong())
		val guild = try {
			runBlocking { lorittaShards.queryCluster(cluster, "/api/v1/guild/${server.guildId}").await().obj }
		} catch (e: PingCommand.ShardOfflineException) {
			return "Something went wrong, sorry."
		}

		variables["serverListConfig"] = server.serverListConfig
		variables["guild"] = guild
		var tagline = server.serverListConfig.tagline ?: ""
		val iconUrl = guild["iconUrl"].nullString
		/* guild.emotes.forEach {
			tagline = tagline.replace(":${it.name}:", "")
		} */
		variables["tagline"] = tagline
		variables["iconUrl"] = iconUrl?.replace("jpg", "png?size=512")
		variables["hasCustomBackground"] = File(Loritta.FRONTEND, "static/assets/img/servers/backgrounds/${server.guildId}.png").exists()

		var userIdentification: TemmieDiscordAuth.UserIdentification? = null

		if (!req.session().isSet("discordAuth")) {
			variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
		} else {
			try {
				val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
				val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)

				variables["selfProfile"] = WebsiteUtils.transformProfileToJson(profile).toString()
			} catch (e: Exception) {
				variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
			}
		}

		val information = transformToJsonObject(guild, server, userIdentification)
		variables["serverInformation"] = GSON.toJson(information)

		return evaluate("partner_view.html", variables)
	}

	companion object {
		fun transformToJsonArray(serverConfigs: List<MongoServerConfig>, userIdentification: TemmieDiscordAuth.UserIdentification?): JsonArray {
			val allServersSample = JsonArray()
			for (server in serverConfigs.toList()) {
				val cluster = DiscordUtils.getLorittaClusterForGuildId(server.guildId.toLong())
				val guild = try {
					runBlocking { lorittaShards.queryCluster(cluster, "/api/v1/guild/${server.guildId}").await().obj }
				} catch (e: PingCommand.ShardOfflineException) {
					continue
				}

				guild["id"].nullString ?: continue // Alguns servidores, por algum motivo, não possuem dono (como?)

				allServersSample.add(transformToJsonObject(guild, server, userIdentification))
			}
			return allServersSample
		}

		fun transformToJsonObject(guild: JsonObject, server: MongoServerConfig, userIdentification: TemmieDiscordAuth.UserIdentification?): JsonObject {
			val guildId = guild["id"].string
			val iconUrl = guild["iconUrl"].nullString
			val name = guild["iconUrl"].string
			val ownerId = guild["ownerId"].string
			val memberCount = guild["count"]["members"].int
			val onlineCount = guild["count"]["onlineMembers"].int
			val timeCreated = guild["timeCreated"].long
			val timeJoined = guild["timeJoined"].long
			val owner = runBlocking { lorittaShards.retrieveUserById(ownerId) }

			val information = JsonObject()
			server.serverListConfig.description = Jsoup.clean(server.serverListConfig.description, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
			server.serverListConfig.tagline = Jsoup.clean(server.serverListConfig.tagline, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
			information["serverListConfig"] = Gson().toJsonTree(server.serverListConfig)
			information["serverListConfig"].obj.remove("votes")
			information["id"] = guildId
			information["iconUrl"] = iconUrl?.replace("jpg", "png")
			information["name"] = name
			information["tagline"] = server.serverListConfig.tagline
			information["description"] = server.serverListConfig.description
			information["keywords"] = Loritta.GSON.toJsonTree(server.serverListConfig.keywords)
			information["ownerId"] = ownerId
			information["ownerName"] = owner?.name
			information["ownerDiscriminator"] = owner?.discriminator
			information["ownerAvatarUrl"] = owner?.effectiveAvatarUrl
			information["memberCount"] = memberCount
			information["onlineCount"] = onlineCount
			information["serverCreatedAt"] = timeCreated
			information["joinedAt"] = timeJoined
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
				val isMember = true

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

			/* guild.emotes.forEach {
				val emote = JsonObject()
				emote["name"] = it.name
				emote["imageUrl"] = it.imageUrl
				serverEmotes.add(emote)
			} */

			information["serverEmotes"] = serverEmotes
			information["joinedServer"] = false // if (userIdentification != null) { guild.getMemberById(userIdentification.id) != null } else { false }
			return information
		}
	}
}