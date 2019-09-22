package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.json.XML
import java.net.InetAddress
import java.util.*

class APILoriDailyRewardStatusView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/daily-reward-status"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
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

		val ips = req.header("X-Forwarded-For").value() // Cloudflare, Apache
		val ip = ips.split(", ")[0]

		// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
		// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
		val lastReceivedDailyAt = transaction(Databases.loritta) {
			com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq userIdentification.id.toLong() }
					.orderBy(Dailies.receivedAt to false)
					.limit(1)
					.firstOrNull()
		}?.get(Dailies.receivedAt) ?: 0L

		val sameIpDailyAt = transaction(Databases.loritta) {
			com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq ip }
					.orderBy(Dailies.receivedAt to false)
					.limit(1)
					.firstOrNull()
		}?.get(Dailies.receivedAt) ?: 0L

		run {
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = lastReceivedDailyAt
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.add(Calendar.DAY_OF_MONTH, 1)
			val tomorrow = calendar.timeInMillis

			if (tomorrow > System.currentTimeMillis() && !loritta.config.isOwner(userIdentification.id.toLong())) {
				val payload = JsonObject()
				payload["canPayoutAgain"] = tomorrow
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				return payload.toString()
			}
		}

		run {
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = sameIpDailyAt
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.add(Calendar.DAY_OF_MONTH, 1)
			val tomorrow = calendar.timeInMillis

			if (tomorrow > System.currentTimeMillis() && !loritta.config.isOwner(userIdentification.id.toLong())) {
				val payload = JsonObject()
				payload["canPayoutAgain"] = tomorrow
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				return payload.toString()
			}
		}

		// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
		// 0 = Stop Forum Spam
		// 1 = Bad hostname
		// 2 = OVH IP

		// Antes de nós realmente decidir "ele deu upvote então vamos dar o upvote", nós iremos verificar o IP no StopForumSpam
		val stopForumSpam = HttpRequest.get("http://api.stopforumspam.org/api?ip=$ip")
				.body()

		// STOP FORUM SPAM
		val xmlJSONObj = XML.toJSONObject(stopForumSpam)

		val response = jsonParser.parse(xmlJSONObj.toString(4)).obj["response"]

		val isSpam = response["appears"].bool

		if (isSpam) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.BAD_IP
			payload["reason"] = 0
			return payload.toString()
		}

		// HOSTNAME BLOCC:tm:
		val addr = InetAddress.getByName(ip)
		val host = addr.hostName.toLowerCase()

		val hostnames = listOf(
				"anchorfree", // Hotspot Shield
				"ipredator.se", // IP redator
				"pixelfucker.org", // Pixelfucker
				"theremailer.net", // TheRemailer
				"tor-exit", // Tor Exit
				"torexit",
				"exitpoint"
		)

		val badHostname = hostnames.any { host.contains(it) }

		if (badHostname) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.BAD_IP
			payload["reason"] = 1
			return payload.toString()
		}

		// OVH BLOCC:tm:
		if (host.matches(Regex(".*ns[0-9]+.*"))) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.BAD_IP
			payload["reason"] = 2
			return payload.toString()
		}

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS

		return gson.toJson(payload)
	}
}