package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.website.LoriWebCode
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.set

object WebsiteUtils {
	private val logger = KotlinLogging.logger {}

	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(code: LoriWebCode, message: String? = null, data: ((JsonObject) -> Unit)? = null): JsonObject {
		val result = jsonObject("error" to createErrorObject(code, message))
		data?.invoke(result)
		return result
	}

	/**
	 * Creates an JSON object containing the code error
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object with the error
	 */
	fun createErrorObject(code: LoriWebCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
				"code" to code.errorId,
				"reason" to code.fancyName,
				"help" to "${loritta.instanceConfig.loritta.website.url}docs/api"
		)

		if (message != null) {
			jsonObject["message"] = message
		}

		return jsonObject
	}

	fun transformToJson(user: User): JsonObject {
		return jsonObject(
				"id" to user.id,
				"name" to user.name,
				"discriminator" to user.discriminator,
				"effectiveAvatarUrl" to user.effectiveAvatarUrl
		)
	}

	fun getServerConfigAsJson(guild: Guild, serverConfig: MongoServerConfig, userIdentification: LorittaJsonWebSession.UserIdentification): JsonElement {
		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val donationKey = transaction(Databases.loritta) {
			loritta.getOrCreateServerConfig(serverConfig.guildId.toLong()).getActiveDonationKeys().firstOrNull()
		}

		if (donationKey != null && donationKey.isActive()) {
			serverConfigJson["donationKey"] = jsonObject(
					"value" to donationKey.value,
					"userId" to donationKey.userId.toString(),
					"expiresAt" to donationKey.expiresAt.toString()
			)
		}

		val textChannels = JsonArray()
		for (textChannel in guild.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name
			json["topic"] = textChannel.topic

			textChannels.add(json)
		}

		serverConfigJson["textChannels"] = textChannels

		val roles = JsonArray()
		for (role in guild.roles) {
			val json = JsonObject()

			json["id"] = role.id
			json["name"] = role.name
			json["isPublicRole"] = role.isPublicRole
			json["isManaged"] = role.isManaged
			json["canInteract"] = guild.selfMember.canInteract(role)

			if (role.color != null) {
				json["color"] = jsonObject(
						"red" to role.color!!.red,
						"green" to role.color!!.green,
						"blue" to role.color!!.blue
				)
			}

			roles.add(json)
		}

		val emotes = JsonArray()
		for (emote in guild.emotes) {
			val json = JsonObject()

			json["id"] = emote.id
			json["name"] = emote.name

			emotes.add(json)
		}

		serverConfigJson["roles"] = roles
		serverConfigJson["emotes"] = emotes
		serverConfigJson["permissions"] = gson.toJsonTree(guild.selfMember.permissions.map { it.name })

		val selfUser = jsonObject(
				"id" to userIdentification.id,
				"name" to userIdentification.username,
				"discriminator" to userIdentification.discriminator,
				"avatar" to "???"
		)
		serverConfigJson["selfUser"] = selfUser

		serverConfigJson["guildName"] = guild.name
		serverConfigJson["memberCount"] = guild.members.size

		return serverConfigJson
	}

	fun getProfileAsJson(profile: Profile): JsonObject {
		return jsonObject(
				"id" to profile.id.value,
				"money" to profile.money
		)
	}

	fun transformProfileToJson(profile: Profile): JsonObject {
		// TODO: É necessário alterar o frontend para usar os novos valores
		val jsonObject = JsonObject()
		jsonObject["userId"] = profile.id.value
		jsonObject["money"] = profile.money
		jsonObject["dreams"] = profile.money // Deprecated
		return jsonObject
	}

	fun getDiscordCrawlerAuthenticationPage(): String {
		return createHTML().html {
			head {
				fun setMetaProperty(property: String, content: String) {
					meta(content = content) { attributes["property"] = property }
				}
				title("Login • Loritta")
				setMetaProperty("og:site_name", "Loritta")
				setMetaProperty("og:title", "Painel da Loritta")
				setMetaProperty("og:description", "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!")
				setMetaProperty("og:image", loritta.instanceConfig.loritta.website.url + "assets/img/loritta_dashboard.png")
				setMetaProperty("og:image:width", "320")
				setMetaProperty("og:ttl", "660")
				setMetaProperty("og:image:width", "320")
				setMetaProperty("theme-color", "#7289da")
				meta("twitter:card", "summary_large_image")
			}
			body {
				p {
					+ "Parabéns, você encontrou um easter egg!"
				}
			}
		}
	}
}