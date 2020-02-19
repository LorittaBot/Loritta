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
import com.mrpowergamerbr.loritta.tables.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import io.ktor.application.ApplicationCall
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit

class GetLoriDailyRewardRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/daily-reward") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = Caffeine.newBuilder()
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build<Long, Mutex>()
				.asMap()
	}

	fun getDailyMultiplier(value: Double): Double {
		return when {
			value >= 179.99 -> 2.0
			value >= 139.99 -> 1.75
			value >= 99.99 -> 1.5
			value >= 59.99 -> 1.25
			else -> 1.0
		}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val recaptcha = call.parameters.get("recaptcha") ?: return

		val body = HttpRequest.get("https://www.google.com/recaptcha/api/siteverify?secret=${loritta.config.googleRecaptcha.serverVoteToken}&response=$recaptcha")
				.body()

		val jsonParser = jsonParser.parse(body).obj

		val success = jsonParser["success"].bool

		if (!success) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.INVALID_CAPTCHA_RESPONSE
			call.respondJson(payload)
			return
		}

		val ip = call.request.trueIp

		val lorittaProfile = loritta.getOrCreateLorittaProfile(userIdentification.id)

		val mutex = mutexes.getOrPut(lorittaProfile.userId) { Mutex() }
		mutex.withLock {
			// Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
			// Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
			val lastReceivedDailyAt = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.receivedById eq lorittaProfile.id.value }
						.orderBy(Dailies.receivedAt to false)
						.limit(1)
						.firstOrNull()
			}?.get(Dailies.receivedAt) ?: 0L

			val sameIpDaily = transaction(Databases.loritta) {
				com.mrpowergamerbr.loritta.tables.Dailies.select { Dailies.ip eq ip }
						.orderBy(Dailies.receivedAt to false)
						.limit(1)
						.firstOrNull()
			}

			val sameIpDailyAt = sameIpDaily?.get(Dailies.receivedAt) ?: 0L

			run {
				val calendar = Calendar.getInstance()
				calendar.timeInMillis = lastReceivedDailyAt
				calendar.set(Calendar.HOUR_OF_DAY, 0)
				calendar.set(Calendar.MINUTE, 0)
				calendar.add(Calendar.DAY_OF_MONTH, 1)
				val tomorrow = calendar.timeInMillis

				if (tomorrow > System.currentTimeMillis() && !loritta.config.isOwner(userIdentification.id.toLong())) {
					val payload = JsonObject()
					payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
					return@withLock payload.toString()
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
					logger.warn { "User ${userIdentification.id} tried to get daily with the same IP as ${sameIpDaily?.get(Dailies.receivedById)}! IP = ${sameIpDaily?.get(Dailies.receivedById)}; Current User Email: ${userIdentification.email}; Daily User Email: ${sameIpDaily?.get(Dailies.email)}" }

					val payload = JsonObject()
					payload["api:code"] = LoriWebCodes.ALREADY_VOTED_TODAY
					return@withLock payload.toString()
				}
			}

			val status = MiscUtils.verifyAccount(userIdentification, ip)
			val email = userIdentification.email
			logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
			logger.info { "Is verified? ${userIdentification.verified}" }
			logger.info { "Email ${email}" }
			logger.info { "IP: $ip" }

			if (!status.canAccess) {
				val payload = JsonObject()
				return@withLock when (status) {
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
				in 8..14 -> 3.0
				in 15..20 -> 4.0
				in 21..25 -> 5.0
				in 26..29 -> 6.0
				else -> 2.0
			}

			val donatorPaid = loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())

			if (donatorPaid != 0.0) {
				when {
					donatorPaid >= 159.99 -> multiplier += 22.55
					donatorPaid >= 139.99 -> multiplier += 16.85
					donatorPaid >= 119.99 -> multiplier += 12.292
					donatorPaid >= 99.99 -> multiplier += 8.634
					donatorPaid >= 79.99 -> multiplier += 5.717
					donatorPaid >= 59.99 -> multiplier += 3.375
					donatorPaid >= 39.99 -> multiplier += 1.5
				}
			}

			var dailyPayout = RANDOM.nextInt(555 /* Math.max(555, 555 * (multiplier - 1)) */, ((600 * multiplier) + 1).toInt()) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams
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
				}.orderBy(DonationKeys.value, SortOrder.DESC)

				val serverConfigs = ServerConfig.wrapRows(results)

				var bestServer: ServerConfig? = null
				var bestServerInfo: JsonObject? = null

				for (config in serverConfigs) {
					logger.info { "Checking ${config.guildId}" }

					val donationValue = config.getActiveDonationKeysValue()
					if (59.99 > donationValue)
						continue

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