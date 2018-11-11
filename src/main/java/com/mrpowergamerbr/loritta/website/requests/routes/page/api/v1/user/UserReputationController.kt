package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.user

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import mu.KotlinLogging
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.Body
import org.jooby.mvc.GET
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/user/:userId/reputation")
class UserReputationController {
	private val logger = KotlinLogging.logger {}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	fun getReputations(req: Request, res: Response) {
		res.type(MediaType.json)
		val receiver = req.param("userId").value()

		val count = transaction(Databases.loritta) {
			Reputations.select { Reputations.receivedById eq receiver.toLong() }.count()
		}

		res.send(jsonObject("count" to count).toString())
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresVariables(true)
	fun giveReputation(req: Request, res: Response, @Body rawMessage: String) {
		res.type(MediaType.json)

		val receiver = req.param("userId").value()
		val userIdentification = req.attributes()["userIdentification"] as TemmieDiscordAuth.UserIdentification? ?: throw WebsiteAPIException(Status.UNAUTHORIZED,
				WebsiteUtils.createErrorPayload(
						LoriWebCode.UNAUTHORIZED
				)
		)

		val json = jsonParser.parse(rawMessage)
		val content = json["content"].string
		val token = json["token"].string
		if (!MiscUtils.checkRecaptcha(Loritta.config.invisibleRecaptchaToken, token))
			throw WebsiteAPIException(
					Status.FORBIDDEN,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.INVALID_RECAPTCHA
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

		val reputations = transaction(Databases.loritta) {
			Reputation.find { Reputations.receivedById eq receiver.toLong() }.sortedByDescending { it.receivedAt }
		}

		res.status(Status.OK)

		val rank = StringBuilder().appendHTML().div(classes = "box-item") {
			val map = reputations.groupingBy { it.givenById }.eachCount()
					.entries
					.sortedByDescending { it.value }

			var idx = 0;
			div(classes = "rank-title") {
				+ "Placar de Reputações"
			}
			table {
				tbody {
					tr {
						th {
							// + "Posição"
						}
						th {}
						th {
							// + "Nome"
						}
					}
					for ((userId, count) in map) {
						if (idx == 5) break;
						val rankUser = lorittaShards.getUserById(userId.toString())

						if (rankUser != null) {
							tr {
								td {
									img(classes = "rank-avatar", src = rankUser.effectiveAvatarUrl) { width = "64" }
								}
								td(classes = "rank-position") {
									+ "#${idx + 1}"
								}
								td {
									if (idx == 0) {
										div(classes = "rank-name rainbow") {
											+ rankUser.name
										}

									} else {
										div(classes = "rank-name") {
											+ rankUser.name
										}
									}
									div(classes = "reputations-received") {
										+ "${count} reputações"
									}
								}
							}
							idx++;
						}
					}
				}
			}
		}

		// Vamos reenviar vários dados utilizados na hora de gerar a telinha
		val response = jsonObject(
				"count" to transaction(Databases.loritta) { Reputations.select { Reputations.receivedById eq receiver.toLong() }.count() },
				"rank" to rank.toString()
		)
		res.send(gson.toJson(response))
	}
}