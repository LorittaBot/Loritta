package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.*

@Path("/api/v1/user/:blah/reputation")
class UserReputationController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	// @LoriRequiresVariables(true)
	fun getReputations(req: Request, res: Response): String {
		logger.info("UserReputationController#getReputations")
		// res.type(MediaType.json)
		val receiver = req.param("blah").value()

		val count = transaction(Databases.loritta) {
			Reputations.select { Reputations.receivedById eq receiver.toLong() }.count()
		}

		// res.send(jsonObject("count" to count).toString())
		return "blah"
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	fun giveReputation(req: Request, res: Response, @Local _userIdentification: TemmieDiscordAuth.UserIdentification?, @Body rawMessage: String) {
		res.type(MediaType.json)

		val receiver = req.param("userId").value()
		val userIdentification = _userIdentification ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.COOLDOWN
				)
		)

		val ip = req.trueIp

		val lastReputationGiven = transaction(Databases.loritta) {
			Reputation.find {
				(Reputations.givenById eq userIdentification.id.toLong()) or
						(Reputations.givenByEmail eq userIdentification.email!!) or
						(Reputations.givenByIp eq ip)
			}.sortedByDescending { it.receivedAt }.firstOrNull()
		}

		val diff = System.currentTimeMillis() - (lastReputationGiven?.receivedAt ?: 0L)

		if (3_600_000 > diff)
			throw WebsiteAPIException(Status.FORBIDDEN,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.COOLDOWN
					)
			)

		val status = MiscUtils.verifyAccount(userIdentification, ip)
		val email = userIdentification.email
		logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
		logger.info { "Is verified? ${userIdentification.verified}" }
		logger.info { "Email ${email}" }
		logger.info { "IP: $ip" }
		MiscUtils.handleVerification(status)

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