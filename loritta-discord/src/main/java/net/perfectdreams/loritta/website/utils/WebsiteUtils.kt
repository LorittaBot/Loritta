package net.perfectdreams.loritta.website.utils

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.ServerConfigs
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
import net.perfectdreams.loritta.dao.ReactionOption
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformers
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.management.ManagementFactory
import java.text.MessageFormat
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object WebsiteUtils {
	val variablesKey = AttributeKey<MutableMap<String, Any?>>("variables")
	val localeKey = AttributeKey<BaseLocale>("locale")

	fun initializeVariables(call: ApplicationCall, locale: BaseLocale, legacyLocale: LegacyBaseLocale, languageCode: String?) {
		val req = call.request
		val attributes = call.attributes

		val variables = mutableMapOf(
				"discordAuth" to null,
				"userIdentification" to null,
				"epochMillis" to System.currentTimeMillis(),
				"guildCount" to lorittaShards.getCachedGuildCount(),
				"availableCommandsCount" to loritta.legacyCommandManager.commandMap.size + loritta.commandManager.commands.size,
				"commandMap" to loritta.legacyCommandManager.commandMap + loritta.commandManager.commands.size,
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

		var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime

		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val correctUrl = LorittaWebsite.WEBSITE_URL.replace("https://", "https://$languageCode.")
		variables["uptimeDays"] = days
		variables["uptimeHours"] = hours
		variables["uptimeMinutes"] = minutes
		variables["uptimeSeconds"] = seconds
		variables["currentUrl"] = correctUrl + req.path().substring(1)

		// Já que Reflection não existe em Kotlin/JS, o Kotlin Serialization não suporta "Any?" em JavaScript.
		// Então vamos fazer algumas pequenas gambiarras para retirar as listas antes de enviar para o website
		val patchedLocales = BaseLocale(locale.id)
		patchedLocales.localeEntries.putAll(locale.localeEntries.filter { it.value is String })
		// Mas nós *precisamos* de listas no Kotlin/JS, então...
		locale.localeEntries.filter { it.value is List<*> }.forEach {
			val value = it.value as List<String>
			// no frontend iremos apenas verificar se começa com "list::" e, se começar, iremos transformar em uma lista
			patchedLocales.localeEntries[it.key] = "list::${value.joinToString("\n")}"
		}

		variables["baseLocale"] = Loritta.GSON.toJson(patchedLocales)
		variables["localeAsJson"] = Loritta.GSON.toJson(legacyLocale.strings)
		variables["websiteUrl"] = LorittaWebsite.WEBSITE_URL
		variables["locale"] = locale

		attributes.put(localeKey, locale)

		for ((key, value) in locale.localeEntries) {
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

		/* val session = call.sessions.get<LorittaJsonWebSession>()
		val discordAuth = session.getDiscordAuthFromJson()

		if (discordAuth != null) {
			try {
				val storedIdMutant = req.session()["discordId"]
				val storedId = if (storedIdMutant.isSet) {
					storedIdMutant.value()
				} else {
					null
				}

				val user = lorittaShards.getUserById(storedId)

				if (forceReauthentication || user == null) {
					discordAuth.isReady(true)
					val userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
					variables["userIdentification"] = userIdentification
					req.set("userIdentification", userIdentification)
					req.session()["discordId"] = userIdentification.id
				} else {
					// Se não estamos forçando a reautenticação, vamos primeiro descobrir se a Lori conhece o usuário, se não, ai a gente irá utilizar a API
					val simpleUserIdentification = SimpleUserIdentification(
							user.name,
							user.id,
							(user.avatarId ?: user.defaultAvatarId),
							user.discriminator
					)

					variables["userIdentification"] = simpleUserIdentification
					req.set("userIdentification", simpleUserIdentification)
				}
				variables["discordAuth"] = discordAuth
				req.set("discordAuth", discordAuth)
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		} */
	}

	fun transformToDashboardConfigurationJson(user: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig): JsonObject {
		val guildJson = jsonObject(
				"name" to guild.name,
				"localeId" to serverConfig.localeId,
				"commandPrefix" to serverConfig.commandPrefix,
				"deleteMessageAfterCommand" to serverConfig.deleteMessageAfterCommand,
				"warnOnMissingPermission" to serverConfig.warnOnMissingPermission,
				"warnOnUnknownCommand" to serverConfig.warnOnUnknownCommand,
				"blacklistedChannels" to serverConfig.blacklistedChannels.toList().toJsonArray(),
				"warnIfBlacklisted" to serverConfig.warnIfBlacklisted,
				"blacklistedWarning" to serverConfig.blacklistedWarning
		)

		val selfMember = WebsiteUtils.transformToJson(lorittaShards.getUserById(user.id)!!)
		selfMember["donationKeys"] = transaction(Databases.loritta) {
			val donationKeys = DonationKey.find {
				DonationKeys.userId eq user.id.toLong()
			}

			jsonArray(
					donationKeys.map {
						val guildUsingKey = ServerConfig.find { ServerConfigs.donationKey eq it.id }.firstOrNull()
						val obj = jsonObject(
								"id" to it.id.value,
								"value" to it.value,
								"expiresAt" to it.expiresAt
						)

						if (guildUsingKey != null) {
							val guild = lorittaShards.getGuildById(guildUsingKey.guildId)

							if (guild != null) {
								obj["usesKey"] = jsonObject(
										"name" to guild.name,
										"iconUrl" to guild.iconUrl
								)
							}
						}

						obj
					}
			)
		}

		guildJson["donationConfig"] = transaction(Databases.loritta) {
			val donationConfig = serverConfig.donationConfig
			jsonObject(
					"customBadge" to (donationConfig?.customBadge ?: false),
					"dailyMultiplier" to (donationConfig?.dailyMultiplier ?: false)
			)
		}

		guildJson["reactionRoleConfigs"] = transaction(Databases.loritta) {
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

		guildJson["levelUpConfig"] = transaction(Databases.loritta) {
			val levelConfig = serverConfig.levelConfig
			val announcements = LevelAnnouncementConfigs.select {
				LevelAnnouncementConfigs.levelConfig eq (levelConfig?.id?.value ?: -1L)
			}

			val announcementArray = jsonArray()
			for (announcement in announcements) {
				announcementArray.add(
						jsonObject(
								"type" to announcement[LevelAnnouncementConfigs.type].toString(),
								"channelId" to announcement[LevelAnnouncementConfigs.channelId]?.toString(),
								"onlyIfUserReceivedRoles" to announcement[LevelAnnouncementConfigs.onlyIfUserReceivedRoles],
								"message" to announcement[LevelAnnouncementConfigs.message].toString()
						)
				)
			}

			val rolesByExperience = RolesByExperience.select {
				RolesByExperience.guildId eq guild.idLong
			}
			val rolesByExperienceArray = jsonArray()
			for (roleByExperience in rolesByExperience) {
				rolesByExperienceArray.add(
						jsonObject(
								"requiredExperience" to roleByExperience[RolesByExperience.requiredExperience].toString(),
								"roles" to roleByExperience[RolesByExperience.roles].map { it.toString() }.toList().toJsonArray()
						)
				)
			}

			val experienceRoleRates = ExperienceRoleRates.select {
				ExperienceRoleRates.guildId eq guild.idLong
			}
			val experienceRoleRatesArray = jsonArray()
			for (experienceRoleRate in experienceRoleRates) {
				experienceRoleRatesArray.add(
						jsonObject(
								"role" to experienceRoleRate[ExperienceRoleRates.role].toString(),
								"rate" to experienceRoleRate[ExperienceRoleRates.rate].toDouble()
						)
				)
			}

			jsonObject(
					"roleGiveType" to (levelConfig?.roleGiveType ?: RoleGiveType.STACK).toString(),
					"noXpChannels" to (levelConfig?.noXpChannels?.toList()?.toJsonArray() ?: jsonArray()),
					"noXpRoles" to (levelConfig?.noXpRoles?.toList()?.toJsonArray() ?: jsonArray()),
					"announcements" to announcementArray,
					"rolesByExperience" to rolesByExperienceArray,
					"experienceRoleRates" to experienceRoleRatesArray
			)
		}

		guildJson["trackedRssFeeds"] = transaction(Databases.loritta) {
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

		transaction(Databases.loritta) {
			val donationKey = serverConfig.donationKey
			if (donationKey != null) {
				guildJson["donationKey"] = jsonObject(
						"id" to donationKey.id.value,
						"value" to donationKey.value,
						"expiresAt" to donationKey.expiresAt,
						"user" to WebsiteUtils.transformToJson(lorittaShards.getUserById(donationKey.userId)!!)
				)
			}
		}

		guildJson["roles"] = guild.roles.map {
			jsonObject(
					"id" to it.id,
					"name" to it.name,
					"colorRaw" to it.colorRaw,
					"canInteract" to guild.selfMember.canInteract(it),
					"isHoisted" to it.isHoisted,
					"isManaged" to it.isManaged
			)
		}.toJsonArray()

		guildJson["textChannels"] = guild.textChannels.map {
			jsonObject(
					"id" to it.id,
					"canTalk" to it.canTalk(),
					"name" to it.name,
					"topic" to it.topic
			)
		}.toJsonArray()

		for (transformer in ConfigTransformers.DEFAULT_TRANSFORMERS)
			guildJson[transformer.configKey] = transformer.toJson(guild, serverConfig)

		return guildJson
	}
}