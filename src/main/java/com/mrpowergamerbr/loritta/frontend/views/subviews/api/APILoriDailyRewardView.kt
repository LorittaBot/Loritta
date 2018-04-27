package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APILoriDailyRewardView : NoVarsView() {
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

		val jsonParser = JSON_PARSER.parse(body).obj

		val success = jsonParser["success"].bool

		if (!success) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INVALID_CAPTCHA_RESPONSE
			return payload.toString()
		}

		val ips = req.header("X-Forwarded-For").value() // Cloudflare, Apache
		val ip = ips.split(", ")[0]

		val lorittaProfile = loritta.getLorittaProfileForUser(userIdentification.id)

		// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
		// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
		val sameIpProfile = loritta.usersColl.find(
				Filters.eq("ip", ip)
		).firstOrNull()

		run {
			val votedAt = lorittaProfile.receivedDailyAt

			val calendar = Calendar.getInstance()
			calendar.timeInMillis = votedAt
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.add(Calendar.DAY_OF_MONTH, 1)
			val tomorrow = calendar.timeInMillis

			val canGetDaily = System.currentTimeMillis() > tomorrow

			if (!canGetDaily) {
				val payload = JsonObject()
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				return payload.toString()
			}
		}

		if (sameIpProfile != null) {
			run {
				val votedAt = sameIpProfile.receivedDailyAt

				val calendar = Calendar.getInstance()
				calendar.timeInMillis = votedAt
				calendar.set(Calendar.HOUR_OF_DAY, 0)
				calendar.set(Calendar.MINUTE, 0)
				calendar.add(Calendar.DAY_OF_MONTH, 1)
				val tomorrow = calendar.timeInMillis

				val canGetDaily = System.currentTimeMillis() > tomorrow

				if (!canGetDaily) {
					val payload = JsonObject()
					payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
					return payload.toString()
				}
			}
		}

		val status = MiscUtils.verifyAccount(userIdentification, ip)

		Loritta.logger.info("AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}")
		Loritta.logger.info("Is verified? ${userIdentification.verified}")
		Loritta.logger.info("Email ${userIdentification.email}")

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
		val multiplier = when (random) {
			in 8..14 -> 3
			in 15..20 -> 4
			in 21..25 -> 5
			in 26..29 -> 6
			else -> 2
		}

		val dailyPayout = RANDOM.nextInt(555 /* Math.max(555, 555 * (multiplier - 1)) */, (555 * multiplier) + 1) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams

		val receivedDailyAt = System.currentTimeMillis()
		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS
		payload["receivedDailyAt"] = receivedDailyAt
		payload["dailyPayout"] = dailyPayout

		lorittaProfile.dreams += dailyPayout
		lorittaProfile.ip = ip
		lorittaProfile.receivedDailyAt = receivedDailyAt

		loritta save lorittaProfile

		Loritta.logger.info("${lorittaProfile.userId} recebeu ${dailyPayout} (quantidade atual: ${lorittaProfile.dreams}) sonhos no Daily! Email: ${userIdentification.email} - IP: ${lorittaProfile.ip}")

		return payload.toString()
	}
}