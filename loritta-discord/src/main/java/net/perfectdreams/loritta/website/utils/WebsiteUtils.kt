package net.perfectdreams.loritta.website.utils

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.OptimizeAssets
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import io.ktor.util.AttributeKey
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ReactionOptions
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedRssFeeds
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import java.text.MessageFormat
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object WebsiteUtils {
	val variablesKey = AttributeKey<MutableMap<String, Any?>>("variables")
	val localeKey = AttributeKey<BaseLocale>("locale")
	val handledStatusBefore = AttributeKey<Boolean>("handledStatusBefore")

	fun initializeVariables(call: ApplicationCall, locale: BaseLocale, legacyLocale: LegacyBaseLocale, languageCode: String?) {
		val req = call.request
		val attributes = call.attributes

		val variables = mutableMapOf(
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

		val hasLangCode = loritta.locales.any { it.value["website.localePath"] == languageCode2 }
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
			if (value is String) {
				variables[key.replace(".", "_")] = MessageFormat.format(value)
			}
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

	fun toSerializable(background: Background) = fromBackgroundToSerializable(background.readValues)

	fun fromBackgroundToSerializable(background: ResultRow) = Background.wrapRow(background).toSerializable()

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

		guildJson["trackedRssFeeds"] = loritta.newSuspendedTransaction {
			val array = JsonArray()

			TrackedRssFeeds.select {
				TrackedRssFeeds.guildId eq guild.idLong
			}.forEach {
				array.add(
						jsonObject(
								"feedUrl" to it[TrackedRssFeeds.feedUrl],
								"channelId" to it[TrackedRssFeeds.channelId],
								"message" to it[TrackedRssFeeds.message]
						)
				)
			}

			array
		}

		guildJson["selfMember"] = selfMember

		for (transformer in ConfigTransformers.ALL_TRANSFORMERS)
			guildJson[transformer.configKey] = transformer.toJson(user, guild, serverConfig)

		return guildJson
	}
}