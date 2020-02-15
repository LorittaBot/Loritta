package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import io.ktor.application.ApplicationCall
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.XML
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeUnit

class GetLoriDailyRewardStatusRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/economy/daily-reward-status") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = Caffeine.newBuilder()
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build<Long, Mutex>()
				.asMap()
	}

	fun getDailyMultiplier(donationKey: DonationKey): Double {
		return when {
			donationKey.value >= 179.99 -> 2.0
			donationKey.value >= 139.99 -> 1.75
			donationKey.value >= 99.99 -> 1.5
			donationKey.value >= 59.99 -> 1.25
			else -> 1.0
		}
	}

	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val session = call.lorittaSession

		var userIdentification: TemmieDiscordAuth.UserIdentification? = null

		val discordAuth = session.getDiscordAuthFromJson()
		if (discordAuth != null) {
			try {
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				call.lorittaSession = session.copy(storedDiscordAuthTokens = null)
			}
		}

		if (userIdentification == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			call.respondJson(payload)
			return
		}

		val ip = call.request.trueIp

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

			if (tomorrow > System.currentTimeMillis() && !com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userIdentification.id.toLong())) {
				val payload = JsonObject()
				payload["canPayoutAgain"] = tomorrow
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				call.respondJson(payload)
				return
			}
		}

		run {
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = sameIpDailyAt
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.add(Calendar.DAY_OF_MONTH, 1)
			val tomorrow = calendar.timeInMillis

			if (tomorrow > System.currentTimeMillis() && !com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userIdentification.id.toLong())) {
				val payload = JsonObject()
				payload["canPayoutAgain"] = tomorrow
				payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
				call.respondJson(payload)
				return
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
			call.respondJson(payload)
			return
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
			call.respondJson(payload)
			return
		}

		// OVH BLOCC:tm:
		if (host.matches(Regex(".*ns[0-9]+.*"))) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.BAD_IP
			payload["reason"] = 2
			call.respondJson(payload)
			return
		}

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS

		call.respondJson(payload)
	}
}