package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.Local
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/user/:userId/reputation")
class UserReputationController {
	private val logger = KotlinLogging.logger {}

	@POST
	@LoriDoNotLocaleRedirect(true)
	fun giveReputation(req: Request, res: Response, @Local userIdentification: TemmieDiscordAuth.UserIdentification?, @Body rawMessage: String) {
		res.type(MediaType.json)
		
		val receiver = req.param("userId").value()

		if (userIdentification == null) {
			res.status(Status.UNAUTHORIZED)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.UNAUTHORIZED
			).toString())
			return
		}

		val lastReputationGiven = transaction(Databases.loritta) {
			Reputation.find { Reputations.receivedById eq userIdentification.id.toLong() }.sortedByDescending { it.receivedAt }.firstOrNull()
		}

		val diff = System.currentTimeMillis() - (lastReputationGiven?.receivedAt ?: 0L)

		if (3_600_000 > diff) {
			res.status(Status.FORBIDDEN)
			res.send(WebsiteUtils.createErrorPayload(
					LoriWebCode.COOLDOWN
			).toString())
			return
		}

		val ip = req.trueIp
		val status = MiscUtils.verifyAccount(userIdentification, ip)
		val email = userIdentification.email
		logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
		logger.info { "Is verified? ${userIdentification.verified}" }
		logger.info { "Email ${email}" }
		logger.info { "IP: $ip" }

		if (!status.canAccess) {
			val payload = JsonObject()
			res.send(when (status) {
				MiscUtils.AccountCheckResult.STOP_FORUM_SPAM,
				MiscUtils.AccountCheckResult.BAD_HOSTNAME,
				MiscUtils.AccountCheckResult.OVH_HOSTNAME -> {
					// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
					// 0 = Stop Forum Spam
					// 1 = Bad hostname
					// 2 = OVH IP
					payload["api:code"] = LoriWebCodes.BAD_IP
					payload["reason"] = when (status) {
						MiscUtils.AccountCheckResult.STOP_FORUM_SPAM -> 0
						MiscUtils.AccountCheckResult.BAD_HOSTNAME -> 1
						MiscUtils.AccountCheckResult.OVH_HOSTNAME -> 2
						else -> -1
					}
				}
				MiscUtils.AccountCheckResult.BAD_EMAIL -> {
					payload["api:code"] = LoriWebCodes.BAD_EMAIL

				}
				MiscUtils.AccountCheckResult.NOT_VERIFIED -> {
					payload["api:code"] = LoriWebCodes.NOT_VERIFIED
				}
				else -> throw RuntimeException("Missing !canAccess result! ${status.name}")
			}.toString())
			return
		}

		val json = jsonParser.parse(rawMessage)
		val content = json["content"].string
		transaction(Databases.loritta) {
			Reputation.new {
				this.givenById = userIdentification.id.toLong()
				this.givenByIp = ip
				this.givenByEmail = userIdentification.email!!
				this.receivedById = receiver.toLong()
				this.content = content
				this.receivedAt = System.currentTimeMillis()
			}
		}

		res.status(Status.NO_CONTENT)
		res.send("")
	}
}