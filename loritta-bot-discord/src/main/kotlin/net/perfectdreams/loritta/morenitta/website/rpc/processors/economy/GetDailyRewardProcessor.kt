package net.perfectdreams.loritta.morenitta.website.rpc.processors.economy

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrowserFingerprints
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxNotifiedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserBoughtBoosterPacks
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.daily.DailyGuildMissingRequirement
import net.perfectdreams.loritta.common.utils.daily.DailyRewardQuestions
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.GuildProfile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.rpc.processors.LorittaRpcProcessor
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Guild
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredDailyRewardSonhosTransaction
import net.perfectdreams.loritta.serializable.requests.GetDailyRewardRequest
import net.perfectdreams.loritta.serializable.responses.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class GetDailyRewardProcessor(val m: LorittaWebsite) : LorittaRpcProcessor {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    val loritta = m.loritta
    private val mutexes = Caffeine.newBuilder()
        .expireAfterAccess(60, TimeUnit.SECONDS)
        .build<Long, Mutex>()
        .asMap()

    suspend fun process(call: ApplicationCall, request: GetDailyRewardRequest): LorittaRPCResponse {
        val dailyMultiplierGuildIdPriority = request.dailyMultiplierGuildIdPriority

        when (val result = getDiscordAccountInformation(m.loritta, call)) {
            LorittaRpcProcessor.DiscordAccountInformationResult.InvalidDiscordAuthorization -> return DiscordAccountError.InvalidDiscordAuthorization()
            LorittaRpcProcessor.DiscordAccountInformationResult.UserIsLorittaBanned -> return DiscordAccountError.UserIsLorittaBanned()
            is LorittaRpcProcessor.DiscordAccountInformationResult.Success -> {
                val (discordAuth, cachedUserIdentification) = result

                val captchaToken = request.captchaToken
                val body = m.loritta.http.submitForm(
                    "https://challenges.cloudflare.com/turnstile/v0/siteverify",
                    Parameters.build {
                        append("secret", loritta.config.loritta.turnstileCaptchas.dailyReward.secretKey)
                        append("response", captchaToken)
                    }
                ).bodyAsText()

                val jsonParser = JsonParser.parseString(body).obj

                val success = jsonParser["success"].nullBool

                if (success == null || !success) {
                    logger.warn { "User ${cachedUserIdentification.id} failed reCAPTCHA! Body: $jsonParser" }
                    return GetDailyRewardResponse.InvalidCaptchaToken()
                }

                val ip = call.request.trueIp

                val lorittaProfile = loritta.getOrCreateLorittaProfile(cachedUserIdentification.id)

                val userIdentification = result.userIdentification

                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                return when (DailyAccountSafetyUtils.verifyIfAccountAndIpAreSafe(m.loritta, userIdentification, ip)) {
                    DailyAccountSafetyUtils.AccountCheckResult.BlockedEmail -> UserVerificationError.BlockedEmail()
                    DailyAccountSafetyUtils.AccountCheckResult.BlockedIp -> UserVerificationError.BlockedIp()
                    DailyAccountSafetyUtils.AccountCheckResult.NotVerified -> UserVerificationError.DiscordAccountNotVerified()
                    DailyAccountSafetyUtils.AccountCheckResult.Safe -> {
                        // If the account is "safe", we will check if the user can get the reward
                        val mutex = mutexes.getOrPut(lorittaProfile.userId) { Mutex() }
                        mutex.withLock {
                            // Para evitar pessoas criando várias contas e votando, nós iremos também verificar o IP dos usuários que votarem
                            // Isto evita pessoas farmando upvotes votando (claro que não é um método infalível, mas é melhor que nada, né?)
                            return when (val payoutResult = DailyAccountSafetyUtils.checkIfUserCanPayout(m.loritta, userIdentification, ip)) {
                                is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameAccount -> DailyPayoutError.AlreadyGotTheDailyRewardSameAccount()
                                is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp -> DailyPayoutError.AlreadyGotTheDailyRewardSameIp()
                                is DailyAccountSafetyUtils.AccountDailyPayoutCheckResult.Success -> {
                                    val email = userIdentification.email

                                    val random = LorittaBot.RANDOM.nextInt(1, 101)
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

                                    val plan = loritta.getUserPremiumPlan(userIdentification.id)

                                    multiplier += plan.dailyMultiplier

                                    var dailyPayout = LorittaBot.RANDOM.nextInt(1800 /* Math.max(555, 555 * (multiplier - 1)) */, ((1800 * multiplier) + 1).toInt()) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams
                                    val originalPayout = dailyPayout

                                    val mutualGuilds = discordAuth.retrieveUserGuilds()

                                    var sponsoredBy: DiscordOAuth2Guild? = null
                                    var multipliedBy: Double? = null
                                    var sponsoredByUserId: Long? = null

                                    val failedDailyServersInfo = mutableListOf<GetDailyRewardResponse.Success.FailedGuild>()

                                    loritta.newSuspendedTransaction {
                                        // Pegar todos os servidores com sonhos patrocinados
                                        val results = (ServerConfigs innerJoin DonationConfigs).selectAll().where {
                                            (ServerConfigs.id inList mutualGuilds.map { it.id.toLong() }) and
                                                    (DonationConfigs.dailyMultiplier eq true)
                                        }

                                        val serverConfigs = ServerConfig.wrapRows(results)

                                        var bestServer: ServerConfig? = null
                                        var bestServerInfo: DiscordOAuth2Guild? = null

                                        // VIVO SPONSOR
                                        val vivoGuildId = 1102914516842446848L
                                        val hasJoinedVivoSponsor = mutualGuilds.any { it.id == vivoGuildId }
                                        if (hasJoinedVivoSponsor) {
                                            bestServerInfo = mutualGuilds.first { it.id == vivoGuildId }

                                            multipliedBy = 2.5
                                            sponsoredBy = bestServerInfo
                                            sponsoredByUserId = null
                                            return@newSuspendedTransaction
                                        }

                                        val reveryGuildId = 1150841346991591515L
                                        val hasJoinedReverySponsor = mutualGuilds.any { it.id == reveryGuildId }
                                        if (hasJoinedReverySponsor && LocalDateTime.of(2026, 2, 21, 0, 0, 0) > now.toLocalDateTime()) {
                                            bestServerInfo = mutualGuilds.first { it.id == reveryGuildId }

                                            multipliedBy = 2.5
                                            sponsoredBy = bestServerInfo
                                            sponsoredByUserId = null
                                            return@newSuspendedTransaction
                                        }

                                        // We are going to sort by the donation value of the server (so a higher plan = more priority) and then by the multiplier guild ID priority
                                        // So if the user has multiple servers that has the best donation key, Loritta will use the Guild ID priority from the URL!
                                        // The value is sorted with a negative sign on front of it, while the priority is sorted by != because we want descending order!
                                        val sortedServers = serverConfigs.map {
                                            Pair(it, ServerPremiumPlans.getPlanFromValue(it.getActiveDonationKeysValueNested()))
                                        }.filter {
                                            it.second.dailyMultiplier > 1.0
                                        }.sortedWith(compareBy({ -it.second.dailyMultiplier }, { it.first.guildId != dailyMultiplierGuildIdPriority }))

                                        for (pair in sortedServers) {
                                            val (config, donationValue) = pair
                                            logger.info { "Checking ${config.guildId}" }

                                            val guild = mutualGuilds.firstOrNull { logger.info { "it[id] = ${it.id}" }; it.id == config.guildId }
                                                ?: continue
                                            val id = guild.id

                                            val xp = GuildProfile.find { (GuildProfiles.guildId eq id) and (GuildProfiles.userId eq userIdentification.id.toLong()) }.firstOrNull()?.xp
                                                ?: 0L

                                            if (500 > xp) {
                                                failedDailyServersInfo.add(
                                                    GetDailyRewardResponse.Success.FailedGuild(
                                                        GetDailyRewardResponse.GuildInfo(
                                                            guild.name,
                                                            guild.icon,
                                                            guild.id
                                                        ),
                                                        DailyGuildMissingRequirement.REQUIRES_MORE_XP,
                                                        500 - xp,
                                                        donationValue.dailyMultiplier
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
                                            val totalDonationValue = ServerPremiumPlans.getPlanFromValue(bestServer.getActiveDonationKeysValueNested())

                                            if (donationConfig != null && donationKey != null) {
                                                multipliedBy = totalDonationValue.dailyMultiplier
                                                sponsoredBy = bestServerInfo
                                                sponsoredByUserId = donationKey.userId
                                            }
                                        }
                                    }

                                    val receivedDailyAt = System.currentTimeMillis()
                                    var sponsoredByData: GetDailyRewardResponse.Success.SonhosSponsor? = null

                                    if (sponsoredBy != null && multipliedBy != null) {
                                        val sponsoredByUser = if (sponsoredByUserId != null) {
                                            HarmonyLoggerFactory.logger {}.value.info { "GetDailyRewardProcessor#retrieveUserInfoById - UserId: ${sponsoredByUserId}" }
                                            val sponsoredByUser = loritta.lorittaShards.retrieveUserInfoById(sponsoredByUserId)

                                            if (sponsoredByUser != null)
                                                GetDailyRewardResponse.UserInfo(
                                                    sponsoredByUser.id,
                                                    sponsoredByUser.name,
                                                    sponsoredByUser.discriminator,
                                                    sponsoredByUser.effectiveAvatarUrl
                                                )
                                            else null
                                        } else null

                                        sponsoredByData = GetDailyRewardResponse.Success.SonhosSponsor(
                                            multipliedBy!!,
                                            GetDailyRewardResponse.GuildInfo(
                                                sponsoredBy!!.name,
                                                sponsoredBy!!.icon,
                                                sponsoredBy!!.id.toLong()
                                            ),
                                            sponsoredByUser,
                                            originalPayout
                                        )

                                        dailyPayout = (dailyPayout * multipliedBy!!).toInt()
                                    }

                                    val userId = userIdentification.id.toLong()
                                    email!!

                                    val dailyPayoutWithoutAnyBonuses = dailyPayout
                                    val bonuses = mutableListOf<GetDailyRewardResponse.Success.DailyPayoutBonus>()
                                    val question = DailyRewardQuestions.all.firstOrNull { it.id == request.questionId }

                                    if (question?.choices?.get(request.answerIndex)?.correctAnswer == true) {
                                        bonuses.add(
                                            GetDailyRewardResponse.Success.DailyPayoutBonus.DailyQuestionBonus(1_000)
                                        )
                                    }

                                    for (bonus in bonuses) {
                                        when (bonus) {
                                            is GetDailyRewardResponse.Success.DailyPayoutBonus.DailyQuestionBonus -> dailyPayout += bonus.quantity
                                        }
                                    }

                                    logger.trace { "userIdentification.id = ${userIdentification.id}" }
                                    logger.trace { "userIdentification.id.toLong() = ${userIdentification.id.toLong()}" }
                                    logger.trace { "receivedDailyAt = $receivedDailyAt" }
                                    logger.trace { "ip = $ip" }
                                    logger.trace { "email = $email" }
                                    logger.trace { "dailyPayout = $dailyPayout" }
                                    logger.trace { "sponsoredBy = $sponsoredBy" }
                                    logger.trace { "multipliedBy = $multipliedBy" }

                                    var loriCoolCardsReceivedBoosterPackResult: GetDailyRewardResponse.Success.LoriCoolCardsEventReward? = null

                                    loritta.newSuspendedTransaction {
                                        val now = Instant.now()
                                        val loriCoolCardsEvent = LoriCoolCardsEvents.selectAll().where {
                                            LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                        }.firstOrNull()

                                        if (loriCoolCardsEvent != null) {
                                            val template = Json.decodeFromString<StickerAlbumTemplate>(loriCoolCardsEvent[LoriCoolCardsEvents.template])

                                            if (template.boosterPacksOnDailyReward != 0) {
                                                repeat(template.boosterPacksOnDailyReward) {
                                                    // If there is an active lori cool cards event, let's add an unopened booster pack to them!
                                                    LoriCoolCardsUserBoughtBoosterPacks.insert {
                                                        it[LoriCoolCardsUserBoughtBoosterPacks.user] = userId
                                                        it[LoriCoolCardsUserBoughtBoosterPacks.event] = loriCoolCardsEvent[LoriCoolCardsEvents.id]
                                                        it[LoriCoolCardsUserBoughtBoosterPacks.boughtAt] = now
                                                    }
                                                }

                                                loriCoolCardsReceivedBoosterPackResult = GetDailyRewardResponse.Success.LoriCoolCardsEventReward(
                                                    loriCoolCardsEvent[LoriCoolCardsEvents.eventName],
                                                    loriCoolCardsEvent[LoriCoolCardsEvents.endsAt].toKotlinInstant(),
                                                    template.boosterPacksOnDailyReward,
                                                    template.stickerPackImageUrl,
                                                    template.sonhosReward
                                                )
                                            }
                                        } else {
                                            loriCoolCardsReceivedBoosterPackResult = null
                                        }

                                        val fingerprintId = BrowserFingerprints.insertAndGetId {
                                            it[BrowserFingerprints.width] = request.fingerprint.width
                                            it[BrowserFingerprints.height] = request.fingerprint.height
                                            it[BrowserFingerprints.availWidth] = request.fingerprint.availWidth
                                            it[BrowserFingerprints.availHeight] = request.fingerprint.availHeight
                                            it[BrowserFingerprints.timezoneOffset] = request.fingerprint.timezoneOffset
                                            it[BrowserFingerprints.contentLanguage] = call.request.acceptLanguage()
                                            it[BrowserFingerprints.accept] = call.request.accept()
                                            it[BrowserFingerprints.clientId] = UUID.fromString(request.fingerprint.clientId)
                                        }

                                        val dailyId = Dailies.insertAndGetId {
                                            it[Dailies.receivedById] = userId
                                            it[Dailies.receivedAt] = receivedDailyAt
                                            it[Dailies.ip] = ip
                                            it[Dailies.email] = email
                                            it[Dailies.userAgent] = call.request.userAgent()
                                            it[Dailies.browserFingerprints] = fingerprintId
                                        }

                                        // Cinnamon transaction log
                                        SimpleSonhosTransactionsLogUtils.insert(
                                            userId,
                                            Instant.ofEpochMilli(receivedDailyAt),
                                            TransactionType.DAILY_REWARD,
                                            dailyPayout.toLong(),
                                            StoredDailyRewardSonhosTransaction(dailyId.value)
                                        )

                                        DailyTaxNotifiedUsers.deleteWhere {
                                            DailyTaxNotifiedUsers.user eq userId
                                        }

                                        lorittaProfile.addSonhosAndAddToTransactionLogNested(
                                            dailyPayout.toLong(),
                                            SonhosPaymentReason.DAILY
                                        )
                                    }

                                    logger.info { "${lorittaProfile.userId} recebeu ${dailyPayout} (quantidade atual: ${lorittaProfile.money}) sonhos no Daily! Email: ${userIdentification.email} - IP: ${ip} - Patrocinado? ${sponsoredBy} ${multipliedBy}" }

                                    // Get current Loritta activity
                                    val gatewayActivity = loritta.loadActivity()

                                    // Check if Loritta's status is a Twitch livestream
                                    var twitchChannelToBeAdvertised: GetDailyRewardResponse.Success.TwitchChannel? = null

                                    if (gatewayActivity != null) {
                                        val type = gatewayActivity.type
                                        val streamUrl = gatewayActivity.streamUrl
                                        if (type == ActivityType.STREAMING && streamUrl != null) {
                                            // Is this really a Twitch stream?
                                            val url = Url(streamUrl)

                                            // VERY VERY VERY hacky
                                            if (url.host.contains("twitch.tv", true)) {
                                                // Yes, it is!
                                                twitchChannelToBeAdvertised = GetDailyRewardResponse.Success.TwitchChannel(url.fullPath.removePrefix("/"))
                                            }
                                        }
                                    }

                                    return GetDailyRewardResponse.Success(
                                        receivedDailyAt,
                                        dailyPayoutWithoutAnyBonuses,
                                        bonuses,
                                        question,
                                        lorittaProfile.money,
                                        sponsoredByData,
                                        failedDailyServersInfo,
                                        twitchChannelToBeAdvertised,
                                        loriCoolCardsReceivedBoosterPackResult
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}