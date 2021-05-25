package net.perfectdreams.loritta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.CustomBadgePayload
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.DailyMultiplierPayload
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.EconomyPayload
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.MiscellaneousPayload
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.ResetXpPayload
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.ActionType
import net.perfectdreams.loritta.utils.auditlog.WebAuditLogUtils
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIGuildAuthRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformers
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PatchServerConfigRoute(loritta: LorittaDiscord) : RequiresAPIGuildAuthRoute(loritta, "/config") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }
		val type = payload["type"].string
		val config = payload["config"].obj

		val transformer = ConfigTransformers.ALL_TRANSFORMERS.firstOrNull { it.payloadType == type }

		if (transformer != null) {
			val guildId = guild.idLong

			transformer.fromJson(userIdentification, guild, serverConfig, config)

			val actionType = WebAuditLogUtils.fromTargetType(type)

			val params = if (actionType == ActionType.UNKNOWN) {
				jsonObject("target_type" to type)
			} else {
				jsonObject()
			}

			loritta.cachedServerConfigs.invalidate(guildId)

			if (actionType != ActionType.RESET_XP) {
				WebAuditLogUtils.addEntry(
						guildId,
						userIdentification.id.toLong(),
						actionType,
						params
				)
			}

			call.respondJson(jsonObject())
		} else {
			val payloadHandlers = mapOf(
					"miscellaneous" to MiscellaneousPayload::class.java,
					"economy" to EconomyPayload::class.java,
					"badge" to CustomBadgePayload::class.java,
					"daily_multiplier" to DailyMultiplierPayload::class.java,
					"reset_xp" to ResetXpPayload::class.java
			)

			val payloadHandlerClass = payloadHandlers[type]

			if (payloadHandlerClass != null) {
				val guildId = guild.idLong

				val payloadHandler = payloadHandlerClass.getDeclaredConstructor().newInstance()
				payloadHandler.process(config, userIdentification, serverConfig, guild)

				val actionType = WebAuditLogUtils.fromTargetType(type)

				val params = if (actionType == ActionType.UNKNOWN) {
					jsonObject("target_type" to type)
				} else {
					jsonObject()
				}

				loritta.cachedServerConfigs.invalidate(guildId)

				if (actionType != ActionType.RESET_XP) {
					WebAuditLogUtils.addEntry(
							guildId,
							userIdentification.id.toLong(),
							actionType,
							params
					)
				}

				val serverConfigJson = WebsiteUtils.transformToDashboardConfigurationJson(
						userIdentification,
						guild,
						serverConfig
				)

				call.respondJson(serverConfigJson)
			} else {
				throw WebsiteAPIException(
						HttpStatusCode.NotImplemented,
						net.perfectdreams.loritta.website.utils.WebsiteUtils.createErrorPayload(
								LoriWebCode.MISSING_PAYLOAD_HANDLER,
								"I don't know how to handle a \"${type}\" payload yet!"
						)
				)
			}
		}
	}
}