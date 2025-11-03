package net.perfectdreams.loritta.morenitta.website.utils

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ReactionOptions
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.ResultRow
import java.text.MessageFormat
import kotlin.io.path.inputStream

object WebsiteUtils {
	// TODO - htmx-adventures: Remove this after we stop using Pebble
	val variablesKey = AttributeKey<MutableMap<String, Any?>>("variables")
	val localeKey = AttributeKey<BaseLocale>("locale")
	val handledStatusBefore = AttributeKey<Boolean>("handledStatusBefore")

	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(loritta: LorittaBot, code: LoriWebCode, message: String? = null, data: ((JsonObject) -> Unit)? = null): JsonObject {
		val result = jsonObject("error" to createErrorObject(loritta, code, message))
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
	fun createErrorObject(loritta: LorittaBot, code: LoriWebCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
			"code" to code.errorId,
			"reason" to code.fancyName,
			"help" to "${loritta.config.loritta.website.url}docs/api"
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

	fun transformToJson(user: CachedUserInfo): JsonObject {
		return jsonObject(
			"id" to user.id,
			"name" to user.name,
			"discriminator" to user.discriminator,
			"effectiveAvatarUrl" to user.effectiveAvatarUrl
		)
	}

	fun getProfileAsJson(profile: Profile): JsonObject {
		return jsonObject(
			"id" to profile.id.value,
			"money" to profile.money
		)
	}

	fun getDiscordCrawlerAuthenticationPage(loritta: LorittaBot): String {
		return createHTML().html {
			head {
				fun setMetaProperty(property: String, content: String) {
					meta(content = content) { attributes["property"] = property }
				}
				title("Login • Loritta")
				setMetaProperty("og:site_name", "Loritta")
				setMetaProperty("og:title", "Painel da Loritta")
				setMetaProperty("og:description", "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!")
				setMetaProperty("og:image", "https://stuff.loritta.website/loritta-and-wumpus-dashboard-yafyr.png")
				setMetaProperty("og:image:width", "320")
				setMetaProperty("og:ttl", "660")
				setMetaProperty("og:image:width", "320")
				setMetaProperty("theme-color", LorittaColors.LorittaAqua.toHex())
				meta("twitter:card", "summary_large_image")
			}
			body {
				p {
					+ "Parabéns, você encontrou um easter egg!"
				}
			}
		}
	}

	fun checkIfAccountHasMFAEnabled(loritta: LorittaBot, userIdentification: TemmieDiscordAuth.UserIdentification): Boolean {
		// This is a security measure, to avoid "high risk" purchases.
		// We will require that users need to verify their account + have MFA enabled.
		if (!userIdentification.verified)
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.UNVERIFIED_ACCOUNT
				)
			)

		if (userIdentification.mfaEnabled == false)
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					loritta,
					LoriWebCode.MFA_DISABLED
				)
			)

		return true
	}

	// TODO - htmx-adventures: Remove this after we stop using Pebble
	fun initializeVariables(loritta: LorittaBot, call: ApplicationCall, locale: BaseLocale, legacyLocale: LegacyBaseLocale, languageCode: String?) {
		val req = call.request
		val attributes = call.attributes

		val variables = mutableMapOf<String, Any?>(
			"discordAuth" to null,
			"userIdentification" to null,
			"epochMillis" to System.currentTimeMillis(),
			"path" to req.path(),
			"clientId" to loritta.config.loritta.discord.applicationId.toString(),
			"environment" to loritta.config.loritta.environment
		)

		attributes.put(variablesKey, variables)

		for ((key, rawMessage) in legacyLocale.strings) {
			variables[key] = MessageFormat.format(rawMessage)
		}

		var pathNoLanguageCode = req.path()
		val split = pathNoLanguageCode.split("/").toMutableList()
		val languageCode2 = split.getOrNull(1)

		val hasLangCode = loritta.localeManager.locales.any { it.value["website.localePath"] == languageCode2 }
		if (hasLangCode) {
			split.removeAt(0)
			split.removeAt(0)
			pathNoLanguageCode = "/" + split.joinToString("/")
		}

		variables["pathNL"] = pathNoLanguageCode // path no language code
		variables["loriUrl"] = LorittaWebsite.WEBSITE_URL + "${languageCode2 ?: "us"}/"

		variables["addBotUrl"] = LorittaDiscordOAuth2AddBotURL(loritta).toString()

		val correctUrl = LorittaWebsite.WEBSITE_URL.replace("https://", "https://$languageCode.")
		variables["currentUrl"] = correctUrl + req.path().substring(1)

		variables["localeAsJson"] = LorittaBot.GSON.toJson(legacyLocale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL
		variables["locale"] = locale

		attributes.put(localeKey, locale)

		for ((key, value) in locale.localeStringEntries) {
			if (value != null)
				variables[key.replace(".", "_")] = MessageFormat.format(value)
		}

		repeat(10) {
			val sponsor = loritta.sponsors.getOrNull(it)

			variables["sponsor_${it}_enabled"] = sponsor != null
			variables["sponsor_${it}_pc_url"] = sponsor?.getRectangularBannerUrl()
			variables["sponsor_${it}_mobile_url"] = sponsor?.getSquareBannerUrl()
			variables["sponsor_${it}_name"] = sponsor?.name
			variables["sponsor_${it}_url"] = sponsor?.link
			variables["sponsor_${it}_slug"] = sponsor?.slug
		}

		val legacyAssets = listOf(
			"assets/css/style.css",
			"assets/js/SpicyMorenitta.js"
		)

		for (asset in legacyAssets) {
			variables["legacy_asset_hash_${asset.split("/").last().split(".").first()}"] = WebsiteAssetsHashes.getLegacyAssetHash(asset)
		}

		variables["legacy_asset_hash_style"] = DigestUtils.md5Hex(LorittaWebsite::class.getPathFromResources("/static/assets/css/style.css")!!.inputStream())
		variables["asset_hash_app"] = LorittaWebsite.INSTANCE.spicyMorenittaBundle.hash()
	}

	suspend fun toSerializable(loritta: LorittaBot, profileDesign: ProfileDesign) = loritta.pudding.transaction { fromProfileDesignToSerializable(loritta, profileDesign.readValues) }
	suspend fun fromProfileDesignToSerializable(loritta: LorittaBot, profileDesign: ResultRow) = loritta.pudding.transaction { ProfileDesign.wrapRow(profileDesign).toSerializable() }

	suspend fun transformToDashboardConfigurationJson(
		loritta: LorittaBot,
		transformers: List<ConfigTransformer>,
		user: LorittaJsonWebSession.UserIdentification,
		guild: Guild,
		serverConfig: ServerConfig
	): JsonObject {
		val guildJson = jsonObject(
			"name" to guild.name
		)

		val selfMember = transformToJson(loritta.lorittaShards.retrieveUserById(user.id)!!)

		guildJson["donationConfig"] = loritta.newSuspendedTransaction {
			val donationConfig = serverConfig.donationConfig
			jsonObject(
				"customBadge" to (donationConfig?.customBadge ?: false),
				"dailyMultiplier" to (donationConfig?.dailyMultiplier ?: false)
			)
		}

		guildJson["reactionRoleConfigs"] = loritta.newSuspendedTransaction {
			val reactionOptions = ReactionOption.find {
				ReactionOptions.guildId eq guild.idLong
			}

			reactionOptions.map {
				jsonObject(
					"textChannelId" to it.textChannelId.toString(),
					"messageId" to it.messageId.toString(),
					"reaction" to it.reaction,
					"locks" to it.locks.toList().toJsonArray(),
					"roleIds" to it.roleIds.toList().toJsonArray()
				)
			}.toJsonArray()
		}

		guildJson["selfMember"] = selfMember

		for (transformer in transformers)
			guildJson[transformer.configKey] = transformer.toJson(user, guild, serverConfig)

		return guildJson
	}

	fun buildAsHtml(originalString: String, onControlChar: (String) -> (Unit), onStringBuild: (String) -> (Unit)) {
		var isControl = false

		val genericStringBuilder = StringBuilder()
		val controlStringBuilder = StringBuilder()

		for (ch in originalString) {
			if (isControl) {
				if (ch == '}') {
					onControlChar.invoke(controlStringBuilder.toString())
					isControl = false
					controlStringBuilder.clear()
					continue
				}

				controlStringBuilder.append(ch)
				continue
			}

			if (ch == '{') {
				onStringBuild.invoke(genericStringBuilder.toString())
				genericStringBuilder.clear()
				isControl = true
				continue
			}

			genericStringBuilder.append(ch)
		}

		onStringBuild.invoke(genericStringBuilder.toString())
	}

	fun convertJDAGuildToSerializable(guild: Guild): DiscordGuild {
		return DiscordGuild(
			guild.idLong,
			guild.name,
			guild.iconId,
			guild.roles.map {
				DiscordRole(
					it.idLong,
					it.name,
					it.colorRaw
				)
			},
			guild.channels.map {
				when (it) {
					is TextChannel -> {
						TextDiscordChannel(
							it.idLong,
							it.name,
							it.canTalk()
						)
					}
					is VoiceChannel -> {
						VoiceDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is Category -> {
						CategoryDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is NewsChannel -> {
						NewsDiscordChannel(
							it.idLong,
							it.name,
							it.canTalk()
						)
					}

					is StageChannel -> {
						StageDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is ForumChannel -> {
						ForumDiscordChannel(
							it.idLong,
							it.name
						)
					}

					else -> UnknownDiscordChannel(
						it.idLong,
						it.name
					)
				}
			},
			guild.emojis.map {
				DiscordEmoji(
					it.idLong,
					it.name,
					it.isAnimated
				)
			}
		)
	}

	fun convertToSerializable(guild: Guild): DiscordGuild {
		return DiscordGuild(
			guild.idLong,
			guild.name,
			guild.iconId,
			guild.roles.map {
				DiscordRole(
					it.idLong,
					it.name,
					it.colorRaw
				)
			},
			guild.channels.map {
				when (it) {
					is TextChannel -> {
						TextDiscordChannel(
							it.idLong,
							it.name,
							it.canTalk()
						)
					}
					is VoiceChannel -> {
						VoiceDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is Category -> {
						CategoryDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is NewsChannel -> {
						NewsDiscordChannel(
							it.idLong,
							it.name,
							it.canTalk()
						)
					}

					is StageChannel -> {
						StageDiscordChannel(
							it.idLong,
							it.name
						)
					}

					is ForumChannel -> {
						ForumDiscordChannel(
							it.idLong,
							it.name
						)
					}

					else -> UnknownDiscordChannel(
						it.idLong,
						it.name
					)
				}
			},
			guild.emojis.map {
				DiscordEmoji(
					it.idLong,
					it.name,
					it.isAnimated
				)
			}
		)
	}

	fun convertJDAUserToSerializable(user: User): DiscordUser {
		return DiscordUser(
			user.idLong,
			user.name,
			user.globalName,
			user.discriminator,
			user.avatarId
		)
	}

	fun convertUserIdentificationToSerializable(user: LorittaJsonWebSession.UserIdentification): DiscordUser {
		return DiscordUser(
			user.id.toLong(),
			user.username,
			user.globalName,
			user.discriminator,
			user.avatar
		)
	}
}