package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.DonationConfigs
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class GetLoriDailyRewardRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/daily-reward") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

		suspend fun checkIfUserCanPayout(userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): Int {
			val todayAtMidnight = Instant.now()
				.atZone(ZoneId.of("America/Sao_Paulo"))
				.toOffsetDateTime()
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.toInstant()
				.toEpochMilli()
			val tomorrowAtMidnight = Instant.now()
				.atZone(ZoneId.of("America/Sao_Paulo"))
				.toOffsetDateTime()
				.plusDays(1)
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.toInstant()
				.toEpochMilli()
			val nowOneHourAgo = Instant.now()
				.atZone(ZoneId.of("America/Sao_Paulo"))
				.toOffsetDateTime()
				.let {
					if (it.hour != 0) // If the hour is 0, we would get the hour *one day ago*, which is isn't what we want
						it.minusHours(1)
					else {
						it.withHour(0)
							.withMinute(0)
							.withSecond(0)
							.withNano(0)
					}
				}
				.toInstant()
				.toEpochMilli()

			// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
			// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
			val lastReceivedDailyAt = loritta.newSuspendedTransaction {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq userIdentification.id.toLong() and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt, SortOrder.DESC)
					.map {
						it[Dailies.receivedAt]
					}
			}

			val sameIpDailyAt = loritta.newSuspendedTransaction {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq ip and (Dailies.receivedAt greaterEq todayAtMidnight) }
					.orderBy(Dailies.receivedAt, SortOrder.DESC)
					.map {
						it[Dailies.receivedAt]
					}
			}

			val sameIpDailyOneHourAgoAt = loritta.newSuspendedTransaction {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq ip and (Dailies.receivedAt greaterEq nowOneHourAgo) }
					.orderBy(Dailies.receivedAt, SortOrder.DESC)
					.map {
						it[Dailies.receivedAt]
					}
			}

			if (lastReceivedDailyAt.isNotEmpty() || sameIpDailyOneHourAgoAt.isNotEmpty()) {
				if (!com.mrpowergamerbr.loritta.utils.loritta.config.isOwner(userIdentification.id.toLong())) {
					throw WebsiteAPIException(
						HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
							LoriWebCode.ALREADY_GOT_THE_DAILY_REWARD_SAME_ACCOUNT_TODAY,
							data = {
								it["canPayoutAgain"] = tomorrowAtMidnight
							}
						)
					)
				}
			}

			if (sameIpDailyAt.isNotEmpty()) {
				// Já pegaram daily no mesmo IP, mas não ativaram 2FA, vamos pedir para o usuário...
				if (userIdentification.mfaEnabled == false) {
					logger.warn { "User ${userIdentification.id} requires 2FA enabled because they have multiple accounts in the same email, but they didn't enable it yet! Asking them to turn it on..." }
					throw WebsiteAPIException(
						HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
							LoriWebCode.MFA_DISABLED
						)
					)
				}

				// Se o IP for IPv4 e tiver mais de uma conta no IP ou se for IPv6 e já tiver qualquer daily na conta
				if (sameIpDailyAt.size >= 3 || ip.contains(":")) {
					throw WebsiteAPIException(
						HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
							LoriWebCode.ALREADY_GOT_THE_DAILY_REWARD_SAME_IP_TODAY,
							data = {
								it["canPayoutAgain"] = tomorrowAtMidnight
								it["detectedIp"] = ip
							}
						)
					)
				}
			}

			return sameIpDailyAt.size
		}

		suspend fun verifyIfAccountAndIpAreSafe(userIdentification: TemmieDiscordAuth.UserIdentification, ip: String) {
			val status = MiscUtils.verifyAccount(userIdentification, ip)
			val email = userIdentification.email
			logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
			logger.info { "Is verified? ${userIdentification.verified}" }
			logger.info { "Email ${email}" }
			logger.info { "IP: $ip" }

			if (!status.canAccess) {
				when (status) {
					MiscUtils.AccountCheckResult.STOP_FORUM_SPAM,
					MiscUtils.AccountCheckResult.BAD_HOSTNAME,
					MiscUtils.AccountCheckResult.OVH_HOSTNAME -> {
						// Para identificar meliantes, cada request terá uma razão determinando porque o IP foi bloqueado
						// 0 = Stop Forum Spam
						// 1 = Bad hostname
						// 2 = OVH IP
						throw WebsiteAPIException(
							HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
								LoriWebCode.BLACKLISTED_IP,
								data = {
									"reason" to when (status) {
										MiscUtils.AccountCheckResult.STOP_FORUM_SPAM -> 0
										MiscUtils.AccountCheckResult.BAD_HOSTNAME -> 1
										MiscUtils.AccountCheckResult.OVH_HOSTNAME -> 2
										else -> -1
									}
								}
							)
						)
					}
					MiscUtils.AccountCheckResult.BAD_EMAIL -> {
						throw WebsiteAPIException(
							HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
								LoriWebCode.BLACKLISTED_EMAIL
							)
						)
					}
					MiscUtils.AccountCheckResult.NOT_VERIFIED -> {
						throw WebsiteAPIException(
							HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
								LoriWebCode.UNVERIFIED_ACCOUNT
							)
						)
					}
					else -> throw RuntimeException("Missing !canAccess result! ${status.name}")
				}
			}
		}
	}

	fun getDailyMultiplier(value: Double) = ServerPremiumPlans.getPlanFromValue(value).dailyMultiplier

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val recaptcha = call.parameters["recaptcha"] ?: return
		val dailyMultiplierGuildIdPriority = call.request.queryParameters["guild"]?.toLongOrNull()

		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${loritta.config.googleRecaptcha.serverVoteToken}&response=$recaptcha")
			.body()

		val jsonParser = JsonParser.parseString(body).obj

		val success = jsonParser["success"].bool

		if (!success) {
			logger.warn { "User ${userIdentification.id} failed reCAPTCHA, error codes: ${jsonParser["error-codes"].array.joinToString(", ")}" }
			throw WebsiteAPIException(
				HttpStatusCode.Forbidden,
				WebsiteUtils.createErrorPayload(
					LoriWebCode.INVALID_RECAPTCHA,
					data = {
						"errorCodes" to jsonParser["error-codes"].array
					}
				)
			)
		}

		val ip = call.request.trueIp

		val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id)

		val userIdentification = discordAuth.getUserIdentification()
		verifyIfAccountAndIpAreSafe(userIdentification, ip)

		val mutex = mutexes.getOrPut(lorittaProfile.userId) { Mutex() }
		mutex.withLock {
			// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
			// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
			checkIfUserCanPayout(userIdentification, ip)

			val status = MiscUtils.verifyAccount(userIdentification, ip)
			val email = userIdentification.email

			val random = RANDOM.nextInt(1, 101)
			var multiplier = when (random) {
				100 -> { // 1
					6.0
				}
				in 94..99 -> { // 3
					5.0
				}
				in 78..93 -> { // 6
					4.0
				}
				in 59..77 -> { // 20
					3.0
				}
				in 34..58 -> { // 25
					2.0
				}
				in 0..33 -> { // 25
					1.5
				}
				else -> 1.1
			}

			val donatorPaid = loritta.getActiveMoneyFromDonationsAsync(userIdentification.id.toLong())
			val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

			multiplier += plan.dailyMultiplier

			var dailyPayout = RANDOM.nextInt(1800 /* Math.max(555, 555 * (multiplier - 1)) */, ((1800 * multiplier) + 1).toInt()) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams
			val originalPayout = dailyPayout

			val mutualGuilds = discordAuth.getUserGuilds()

			var sponsoredBy: TemmieDiscordAuth.Guild? = null
			var multipliedBy: Double? = null
			var sponsoredByUserId: Long? = null

			val failedDailyServersInfo = jsonArray()

			loritta.newSuspendedTransaction {
				// Pegar todos os servidores com sonhos patrocinados
				val results = (ServerConfigs innerJoin DonationConfigs).select {
					(ServerConfigs.id inList mutualGuilds.map { it.id.toLong() }) and
							(DonationConfigs.dailyMultiplier eq true)
				}

				val serverConfigs = ServerConfig.wrapRows(results)

				var bestServer: ServerConfig? = null
				var bestServerInfo: TemmieDiscordAuth.Guild? = null

				// We are going to sort by the donation value of the server (so a higher plan = more priority) and then by the multiplier guild ID priority
				// So if the user has multiple servers that has the best donation key, Loritta will use the Guild ID priority from the URL!
				// The value is sorted with a negative sign on front of it, while the priority is sorted by != because we want descending order!
				val sortedServers = serverConfigs.map {
					Pair(it, it.getActiveDonationKeysValueNested())
				}.filter {
					ServerPremiumPlans.getPlanFromValue(it.second).dailyMultiplier > 1.0
				}.sortedWith(compareBy({ -it.second }, { it.first.guildId != dailyMultiplierGuildIdPriority }))

				for (pair in sortedServers) {
					val (config, donationValue) = pair
					logger.info { "Checking ${config.guildId}" }

					val guild = mutualGuilds.firstOrNull { logger.info { "it[id] = ${it.id.toLong()}" }; it.id.toLong() == config.guildId }
						?: continue
					val id = guild.id.toLong()

					val xp = GuildProfile.find { (GuildProfiles.guildId eq id) and (GuildProfiles.userId eq userIdentification.id.toLong()) }.firstOrNull()?.xp
						?: 0L

					if (500 > xp) {
						failedDailyServersInfo.add(
							jsonObject(
								"guild" to jsonObject(
									"name" to guild.name,
									"iconUrl" to guild.icon,
									"id" to guild.id
								),
								"type" to DailyGuildMissingRequirement.REQUIRES_MORE_XP.toString(),
								"data" to 500 - xp,
								"multiplier" to getDailyMultiplier(donationValue)
							)
						)
						continue
					}

					bestServer = config
					bestServerInfo = guild
					break
				}

				if (bestServer != null) {
					val donationConfig = bestServer.donationConfig
					val donationKey = bestServer.getActiveDonationKeysNested().firstOrNull()
					val totalDonationValue = bestServer.getActiveDonationKeysValueNested()

					if (donationConfig != null && donationKey != null) {
						multipliedBy = getDailyMultiplier(totalDonationValue)
						sponsoredBy = bestServerInfo
						sponsoredByUserId = donationKey.userId
					}
				}
			}

			val receivedDailyAt = System.currentTimeMillis()
			val payload = JsonObject()

			if (sponsoredBy != null && multipliedBy != null) {
				val sponsor = jsonObject(
					"multipliedBy" to multipliedBy,
					"guild" to jsonObject(
						"name" to sponsoredBy!!.name,
						"iconUrl" to sponsoredBy!!.icon,
						"id" to sponsoredBy!!.id
					),
					"originalPayout" to originalPayout
				)

				if (sponsoredByUserId != null) {
					val sponsoredByUser = lorittaShards.retrieveUserInfoById(sponsoredByUserId)

					if (sponsoredByUser != null)
						sponsor["user"] = WebsiteUtils.transformToJson(sponsoredByUser)
				}

				payload["sponsoredBy"] = sponsor

				dailyPayout = (dailyPayout * multipliedBy!!).toInt()
			}

			val id = userIdentification.id.toLong()
			email!!

			logger.trace { "userIdentification.id = ${userIdentification.id}" }
			logger.trace { "userIdentification.id.toLong() = ${userIdentification.id.toLong()}" }
			logger.trace { "receivedDailyAt = $receivedDailyAt" }
			logger.trace { "ip = $ip" }
			logger.trace { "email = $email" }
			logger.trace { "dailyPayout = $dailyPayout" }
			logger.trace { "sponsoredBy = $sponsoredBy" }
			logger.trace { "multipliedBy = $multipliedBy" }

			loritta.newSuspendedTransaction {
				Dailies.insert {
					it[Dailies.receivedById] = id
					it[Dailies.receivedAt] = receivedDailyAt
					it[Dailies.ip] = ip
					it[Dailies.email] = email
					it[Dailies.userAgent] = call.request.userAgent()
				}

				lorittaProfile.addSonhosAndAddToTransactionLogNested(
					dailyPayout.toLong(),
					SonhosPaymentReason.DAILY
				)
			}

			payload["receivedDailyAt"] = receivedDailyAt
			payload["dailyPayout"] = dailyPayout
			payload["currentBalance"] = lorittaProfile.money
			payload["failedGuilds"] = failedDailyServersInfo

			logger.info { "${lorittaProfile.userId} recebeu ${dailyPayout} (quantidade atual: ${lorittaProfile.money}) sonhos no Daily! Email: ${userIdentification.email} - IP: ${ip} - Patrocinado? ${sponsoredBy} ${multipliedBy}" }
			call.respondJson(payload)
		}
	}
}