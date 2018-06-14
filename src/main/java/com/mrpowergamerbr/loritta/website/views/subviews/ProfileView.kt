package com.mrpowergamerbr.loritta.website.views.subviews

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.website.evaluate
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.Guild
import org.jooby.Request
import org.jooby.Response
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class ProfileView : AbstractView() {
	override fun handleRender(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): Boolean {
		val arg0 = path.split("/").getOrNull(2)
		val user = try {
			lorittaShards.getUserById(arg0)
		} catch (e: Exception) {
			null
		}
		return user != null && path.startsWith("/profile/")
	}

	override fun render(req: Request, res: Response, path: String, variables: MutableMap<String, Any?>): String {
		val arg0 = path.split("/").getOrNull(2) ?: "derp"
		val arg3 = path.split("/").getOrNull(3)
		val user = lorittaShards.getUserById(arg0)!!
		val lorittaProfile = loritta.getLorittaProfileForUser(arg0)

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

		variables["profileUser"] = user
		variables["lorittaProfile"] = lorittaProfile

		if (arg3 == "config" && userIdentification?.id == user.id) {
			return evaluate("profile_dashboard.html", variables)
		}

		variables["badgesBase64"] = PerfilCommand.getUserBadges(user).map {
			val baos = ByteArrayOutputStream()
			ImageIO.write(it, "png", baos)
			Base64.getEncoder().encodeToString(baos.toByteArray())
		}

		val favoriteEmotes = lorittaProfile.usedEmotes.entries.sortedByDescending { it.value }
		val emotes = mutableListOf<Emote>()

		for (favoriteEmote in favoriteEmotes) {
			if (emotes.size > 34)
				break

			val emote = lorittaShards.getEmoteById(favoriteEmote.key)
			if (emote != null)
				emotes.add(emote)
		}

		variables["favoriteEmotes"] = emotes

		val mutualGuilds = lorittaShards.getMutualGuilds(user).sortedByDescending { it.members.size }

		val serverConfigs = loritta.serversColl.find(
				Filters.`in`("_id", mutualGuilds.map { it.id })
		).toMutableList()

		variables["mutualGuilds"] = mutualGuilds

		val ownerOfGuilds = mutableListOf<Guild>()
		val inGuildListEnabled = mutableListOf<Guild>()
		val notInGuildList = mutableListOf<Guild>()
		val onlineCount = mutableMapOf<Guild, Int>()

		for (guild in mutualGuilds) {
			val serverConfig = serverConfigs.firstOrNull { it.guildId == guild.id } ?: continue

			if (guild.owner.user == user && serverConfig.serverListConfig.isEnabled) {
				ownerOfGuilds.add(guild)
				onlineCount[guild] = guild.members.count { it.onlineStatus != OnlineStatus.OFFLINE }
			}
			if (serverConfig.serverListConfig.isEnabled) {
				inGuildListEnabled.add(guild)
			} else {
				notInGuildList.add(guild)
			}
		}

		variables["ownerOfGuilds"] = ownerOfGuilds.sortedByDescending { it.members.size }
		variables["inGuildListEnabled"] = inGuildListEnabled
		variables["notInGuildList"] = notInGuildList
		variables["onlineCount"] = onlineCount

		return evaluate("profile.html", variables)
	}
}