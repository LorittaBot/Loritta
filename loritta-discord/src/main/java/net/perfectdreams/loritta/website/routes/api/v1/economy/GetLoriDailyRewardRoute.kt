package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Dailies
import com.mrpowergamerbr.loritta.tables.DonationConfigs
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Requires2FAChecksUsers
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import net.perfectdreams.loritta.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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

		fun checkIfUserCanPayout(userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): Int {
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

			// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
			// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
			val lastReceivedDailyAt = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq userIdentification.id.toLong() and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(Dailies.receivedAt to false)
						.map {
							it[Dailies.receivedAt]
						}
			}

			val sameIpDailyAt = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq ip and (Dailies.receivedAt greaterEq todayAtMidnight) }
						.orderBy(Dailies.receivedAt to false)
						.map {
							it[Dailies.receivedAt]
						}
			}

			if (lastReceivedDailyAt.isNotEmpty()) {
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
				if (userIdentification.mfaEnabled == false)
					throw WebsiteAPIException(
							HttpStatusCode.Forbidden,
							WebsiteUtils.createErrorPayload(
									LoriWebCode.MFA_DISABLED
							)
					)

				if (sameIpDailyAt.size >= 3) {
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

			val requires2FA = transaction(Databases.loritta) {
				Requires2FAChecksUsers.select { Requires2FAChecksUsers.userId eq userIdentification.id.toLong() }.count() != 0
			}

			if (requires2FA && userIdentification.mfaEnabled == false)
				throw WebsiteAPIException(
						HttpStatusCode.Forbidden,
						WebsiteUtils.createErrorPayload(
								LoriWebCode.MFA_DISABLED
						)
				)

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
				}.toString()
			}
		}
	}

	fun getDailyMultiplier(value: Double) = ServerPremiumPlans.getPlanFromValue(value).dailyMultiplier

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val recaptcha = call.parameters["recaptcha"] ?: return

		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${loritta.config.googleRecaptcha.serverVoteToken}&response=$recaptcha")
				.body()

		val jsonParser = jsonParser.parse(body).obj

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

			val donatorPaid = loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())
			val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

			multiplier += plan.dailyMultiplier

			var dailyPayout = RANDOM.nextInt(1800 /* Math.max(555, 555 * (multiplier - 1)) */, ((1800 * multiplier) + 1).toInt()) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams
			val originalPayout = dailyPayout

			val mutualGuilds = lorittaShards.queryMutualGuildsInAllLorittaClusters(userIdentification.id)

			var sponsoredBy: JsonObject? = null
			var multipliedBy: Double? = null
			var sponsoredByUserId: Long? = null

			val failedDailyServersInfo = jsonArray()
			val user =  lorittaShards.retrieveUserById(userIdentification.id)

			transaction(Databases.loritta) {
				// Pegar todos os servidores com sonhos patrocinados
				val results = (ServerConfigs innerJoin DonationConfigs).select {
					(ServerConfigs.id inList mutualGuilds.map { it["id"].string.toLong() }) and
							(DonationConfigs.dailyMultiplier eq true)
				}

				val serverConfigs = ServerConfig.wrapRows(results)

				var bestServer: ServerConfig? = null
				var bestServerInfo: JsonObject? = null

				for (pair in serverConfigs.map { Pair(it, it.getActiveDonationKeysValue()) }.filter { it.second > 59.99 }.sortedByDescending { it.second  }) {
					val (config, donationValue) = pair
					logger.info { "Checking ${config.guildId}" }

					val guild = mutualGuilds.firstOrNull { logger.info { "it[id] = ${it["id"].string.toLong()}" }; it["id"].string.toLong() == config.guildId }?.obj
							?: continue
					val id = guild["id"].string.toLong()

					val epochMillis = guild["timeJoined"].long

					val requiredTime = if (user?.avatarId == null)
						1_296_000_000
					else
						Constants.ONE_WEEK_IN_MILLISECONDS

					if (epochMillis + requiredTime > System.currentTimeMillis()) { // 15 dias
						val diff = epochMillis + requiredTime - System.currentTimeMillis()
						failedDailyServersInfo.add(
								jsonObject(
										"guild" to jsonObject(
												"name" to guild["name"].string,
												"iconUrl" to guild["iconUrl"].nullString,
												"id" to guild["id"].string
										),
										"type" to DailyGuildMissingRequirement.REQUIRES_MORE_TIME.toString(),
										"data" to diff,
										"multiplier" to getDailyMultiplier(donationValue)
								)
						)
						continue
					}

					val xp = GuildProfile.find { (GuildProfiles.guildId eq id) and (GuildProfiles.userId eq userIdentification.id.toLong()) }.firstOrNull()?.xp
							?: 0L

					if (500 > xp) {
						failedDailyServersInfo.add(
								jsonObject(
										"guild" to jsonObject(
												"name" to guild["name"].string,
												"iconUrl" to guild["iconUrl"].nullString,
												"id" to guild["id"].string
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
					val donationKey = bestServer.getActiveDonationKeys().firstOrNull()
					val totalDonationValue = bestServer.getActiveDonationKeysValue()

					if (donationConfig != null && donationKey != null && totalDonationValue >= 59.99) {
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
								"name" to sponsoredBy!!["name"].nullString,
								"iconUrl" to sponsoredBy!!["iconUrl"].nullString,
								"id" to sponsoredBy!!["id"].nullString
						),
						"originalPayout" to originalPayout
				)

				if (sponsoredByUserId != null) {
					val sponsoredByUser = lorittaShards.retrieveUserById(sponsoredByUserId)

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

			transaction(Databases.loritta) {
				Dailies.insert {
					it[Dailies.receivedById] = id
					it[Dailies.receivedAt] = receivedDailyAt
					it[Dailies.ip] = ip
					it[Dailies.email] = email
				}

				lorittaProfile.money += dailyPayout

				SonhosTransaction.insert {
					it[givenBy] = null
					it[receivedBy] = lorittaProfile.id.value
					it[givenAt] = System.currentTimeMillis()
					it[quantity] = dailyPayout.toBigDecimal()
					it[reason] = SonhosPaymentReason.DAILY
				}
			}

			payload["api:code"] = LoriWebCodes.SUCCESS
			payload["receivedDailyAt"] = receivedDailyAt
			payload["dailyPayout"] = dailyPayout
			payload["currentBalance"] = lorittaProfile.money
			payload["failedGuilds"] = failedDailyServersInfo

			logger.info { "${lorittaProfile.userId} recebeu ${dailyPayout} (quantidade atual: ${lorittaProfile.money}) sonhos no Daily! Email: ${userIdentification.email} - IP: ${ip} - Patrocinado? ${sponsoredBy} ${multipliedBy}" }
			call.respondJson(payload)
		}
	}
}