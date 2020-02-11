package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.ActionType
import net.perfectdreams.loritta.utils.auditlog.WebAuditLogUtils
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.*

@Path("/api/v1/guild/:guildId/config")
class UpdateServerConfigController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun getConfig(req: Request, res: Response, guildId: String, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Local newServerConfig: ServerConfig, @Local serverConfig: MongoServerConfig, @Local guild: Guild) {
		res.type(MediaType.json)

		val serverConfigJson = Gson().toJson(
				WebsiteUtils.transformToDashboardConfigurationJson(
						userIdentification,
						guild,
						newServerConfig,
						serverConfig
				)
		)

		res.send(serverConfigJson)
	}

	@PATCH
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun patchConfig(req: Request, res: Response, guildId: String, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Local guild: Guild, @Local newServerConfig: ServerConfig, @Local serverConfig: MongoServerConfig, @Body rawConfig: String) {
		res.type(MediaType.json)

		val payload = jsonParser.parse(rawConfig).obj
		val type = payload["type"].string
		val config = payload["config"].obj

		val payloadHandlers = mapOf(
				"moderation" to ModerationPayload::class.java,
				"autorole" to AutorolePayload::class.java,
				"welcomer" to WelcomerPayload::class.java,
				"miscellaneous" to MiscellaneousPayload::class.java,
				"economy" to EconomyPayload::class.java,
				"text_channels" to TextChannelsPayload::class.java,
				"timers" to TimersPayload::class.java,
				"premium" to PremiumKeyPayload::class.java,
				"badge" to CustomBadgePayload::class.java,
				"daily_multiplier" to DailyMultiplierPayload::class.java,
				"level" to LevelPayload::class.java,
				"reset_xp" to ResetXpPayload::class.java,
				"twitter" to TwitterPayload::class.java,
				"rss_feeds" to RssFeedsPayload::class.java,
				"default" to GeneralConfigPayload::class.java
		)

		val payloadHandlerClass = payloadHandlers[type]

		if (payloadHandlerClass != null) {
			val payloadHandler = payloadHandlerClass.getDeclaredConstructor().newInstance()
			payloadHandler.process(config, userIdentification, newServerConfig, serverConfig, guild)

			val actionType = WebAuditLogUtils.fromTargetType(type)

			val params = if (actionType == ActionType.UNKNOWN) {
				jsonObject("target_type" to type)
			} else {
				jsonObject()
			}

			if (actionType != ActionType.RESET_XP) {
				WebAuditLogUtils.addEntry(
						guildId.toLong(),
						userIdentification.id.toLong(),
						actionType,
						params
				)
			}

			loritta save serverConfig
			res.status(Status.OK)
			res.send(
					WebsiteUtils.transformToDashboardConfigurationJson(
							userIdentification,
							guild,
							newServerConfig,
							serverConfig
					)
			)
		} else {
			res.status(Status.NOT_IMPLEMENTED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.MISSING_PAYLOAD_HANDLER,
							"I don't know how to handle a \"${type}\" payload yet!"
					)
			)
			return
		}
	}
}