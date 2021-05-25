package net.perfectdreams.loritta.website.utils

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ProfileDesign
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssets
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.*
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ReactionOptions
import net.perfectdreams.loritta.utils.CachedUserInfo
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformers
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.MessageFormat
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object WebsiteUtils {
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

	fun checkIfAccountHasMFAEnabled(userIdentification: TemmieDiscordAuth.UserIdentification): Boolean {
		// This is a security measure, to avoid "high risk" purchases.
		// We will require that users need to verify their account + have MFA enabled.
		if (!userIdentification.verified)
			throw WebsiteAPIException(
					HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.UNVERIFIED_ACCOUNT
					)
			)

		if (userIdentification.mfaEnabled == false)
			throw WebsiteAPIException(
					HttpStatusCode.Forbidden,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.MFA_DISABLED
					)
			)

		return true
	}

	fun initializeVariables(call: ApplicationCall, locale: BaseLocale, legacyLocale: LegacyBaseLocale, languageCode: String?) {
		val req = call.request
		val attributes = call.attributes

		val variables = mutableMapOf<String, Any?>(
				"discordAuth" to null,
				"userIdentification" to null,
				"epochMillis" to System.currentTimeMillis(),
				"path" to req.path(),
				"clientId" to loritta.discordConfig.discord.clientId,
				"cssAssetVersion" to OptimizeAssets.cssAssetVersion,
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

		variables["addBotUrl"] = loritta.discordInstanceConfig.discord.addBotUrl

		val correctUrl = LorittaWebsite.WEBSITE_URL.replace("https://", "https://$languageCode.")
		variables["currentUrl"] = correctUrl + req.path().substring(1)

		variables["localeAsJson"] = Loritta.GSON.toJson(legacyLocale.strings)
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

		variables["asset_hash_app"] = WebsiteAssetsHashes.getAssetHash("assets/js/app.js")
	}

	fun toSerializable(background: Background) = transaction(Databases.loritta) { fromBackgroundToSerializable(background.readValues) }
	fun fromBackgroundToSerializable(background: ResultRow) = transaction(Databases.loritta) { Background.wrapRow(background).toSerializable() }
	fun toSerializable(profileDesign: ProfileDesign) = transaction(Databases.loritta) { fromProfileDesignToSerializable(profileDesign.readValues) }
	fun fromProfileDesignToSerializable(profileDesign: ResultRow) = transaction(Databases.loritta) { ProfileDesign.wrapRow(profileDesign).toSerializable() }

	suspend fun transformToDashboardConfigurationJson(user: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig): JsonObject {
		val guildJson = jsonObject(
				"name" to guild.name
		)

		val selfMember = WebsiteUtils.transformToJson(lorittaShards.retrieveUserById(user.id)!!)

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

		for (transformer in ConfigTransformers.ALL_TRANSFORMERS)
			guildJson[transformer.configKey] = transformer.toJson(user, guild, serverConfig)

		return guildJson
	}
}