package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.dao.Daily
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APILoriDailyRewardView : NoVarsView() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/daily-reward"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val recaptcha = req.param("recaptcha").value()
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${Loritta.config.recaptchaToken}&response=$recaptcha")
				.body()

		val jsonParser = jsonParser.parse(body).obj

		val success = jsonParser["success"].bool

		if (!success) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INVALID_CAPTCHA_RESPONSE
			return payload.toString()
		}

		val ips = req.header("X-Forwarded-For").value() // Cloudflare, Apache
		val ip = ips.split(", ")[0]

		val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id)

		// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
		// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = System.currentTimeMillis()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.add(Calendar.DAY_OF_MONTH, 1)
		val tomorrow = calendar.timeInMillis

		val currentDaily = transaction(Databases.loritta) { Daily.find { (Dailies.receivedById eq userIdentification.id.toLong()) and (Dailies.receivedAt less tomorrow) }.firstOrNull() }
		val sameIpDaily = transaction(Databases.loritta) { Daily.find { (Dailies.ip eq ip) and (Dailies.receivedAt less tomorrow) }.firstOrNull() }

		if (currentDaily != null || sameIpDaily != null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
			return payload.toString()
		}

		val status = MiscUtils.verifyAccount(userIdentification, ip)
		val email = userIdentification.email
		logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
		logger.info { "Is verified? ${userIdentification.verified}" }
		logger.info { "Email ${email}" }
		logger.info { "IP: $ip" }

		if (!status.canAccess) {
			val payload = JsonObject()
			return when (status) {
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
			}.toString()
		}

		val random = RANDOM.nextInt(0, 30)
		var multiplier = when (random) {
			in 8..14 -> 3
			in 15..20 -> 4
			in 21..25 -> 5
			in 26..29 -> 6
			else -> 2
		}

		val isDonator = lorittaProfile.isDonator && lorittaProfile.donationExpiresIn > System.currentTimeMillis()
		if (isDonator) {
			when {
				lorittaProfile.donatorPaid >= 79.99 -> multiplier += 4
				lorittaProfile.donatorPaid >= 59.99 -> multiplier += 3
				lorittaProfile.donatorPaid >= 39.99 -> multiplier += 2
				lorittaProfile.donatorPaid >= 19.99 -> multiplier += 1
			}
		}

		val dailyPayout = RANDOM.nextInt(555 /* Math.max(555, 555 * (multiplier - 1)) */, (555 * multiplier) + 1) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams

		val receivedDailyAt = System.currentTimeMillis()
		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS
		payload["receivedDailyAt"] = receivedDailyAt
		payload["dailyPayout"] = dailyPayout

		val id = userIdentification.id.toLong()

		println(userIdentification.id.toLong())
		println(receivedDailyAt)
		println(ip)
		println(email!!)

		transaction(Databases.loritta) {
			Dailies.insert {
				it[Dailies.receivedById] = id
				it[Dailies.receivedAt] = receivedDailyAt
				it[Dailies.ip] = ip
				it[Dailies.email] = email
			}

			lorittaProfile.money += dailyPayout
		}


		logger.info { "${lorittaProfile.userId} recebeu ${dailyPayout} (quantidade atual: ${lorittaProfile.money}) sonhos no Daily! Email: ${userIdentification.email} - IP: ${ip}" }
		return payload.toString()
	}
}