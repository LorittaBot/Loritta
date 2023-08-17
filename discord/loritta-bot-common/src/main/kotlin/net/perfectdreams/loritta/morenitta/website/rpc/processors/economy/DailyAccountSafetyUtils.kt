package net.perfectdreams.loritta.morenitta.website.rpc.processors.economy

import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.ZonedDateTime

object DailyAccountSafetyUtils {
    private val logger = KotlinLogging.logger {}

    suspend fun checkIfUserCanPayout(loritta: LorittaBot, userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): AccountDailyPayoutCheckResult {
        val todayAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()
        val tomorrowAtMidnight = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
            .plusDays(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()
        val nowOneHourAgo = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
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
            net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies.select { Dailies.receivedById eq userIdentification.id.toLong() and (Dailies.receivedAt greaterEq todayAtMidnight) }.orderBy(
                Dailies.receivedAt, SortOrder.DESC)
                .map {
                    it[Dailies.receivedAt]
                }
        }

        val sameIpDailyAt = loritta.newSuspendedTransaction {
            net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies.select { Dailies.ip eq ip and (Dailies.receivedAt greaterEq todayAtMidnight) }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .map {
                    it[Dailies.receivedAt]
                }
        }

        val sameIpDailyOneHourAgoAt = loritta.newSuspendedTransaction {
            net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies.select { Dailies.ip eq ip and (Dailies.receivedAt greaterEq nowOneHourAgo) }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .map {
                    it[Dailies.receivedAt]
                }
        }

        if (lastReceivedDailyAt.isNotEmpty() || sameIpDailyOneHourAgoAt.isNotEmpty()) {
            if (!loritta.isOwner(userIdentification.id.toLong())) {
                return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameAccount(tomorrowAtMidnight)
            }
        }

        if (sameIpDailyAt.isNotEmpty()) {
            // Se o IP for IPv4 e tiver mais de uma conta no IP ou se for IPv6 e já tiver qualquer daily na conta
            if (sameIpDailyAt.size >= 3 || ip.contains(":")) {
                return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp(
                    tomorrowAtMidnight,
                    ip
                )
            }
        }

        return AccountDailyPayoutCheckResult.Success(sameIpDailyAt.size)
    }

    suspend fun verifyIfAccountAndIpAreSafe(loritta: LorittaBot, userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): AccountCheckResult {
        val status = MiscUtils.verifyAccount(loritta, userIdentification, ip)
        val email = userIdentification.email
        logger.info { "AccountCheckResult for (${userIdentification.username}#${userIdentification.discriminator}) ${userIdentification.id} - ${status.name}" }
        logger.info { "Is verified? ${userIdentification.verified}" }
        logger.info { "Email ${email}" }
        logger.info { "IP: $ip" }

        return when (status) {
            MiscUtils.AccountCheckResult.STOP_FORUM_SPAM,
            MiscUtils.AccountCheckResult.BAD_HOSTNAME,
            MiscUtils.AccountCheckResult.OVH_HOSTNAME -> AccountCheckResult.BlockedIp

            MiscUtils.AccountCheckResult.BAD_EMAIL -> AccountCheckResult.BlockedEmail

            MiscUtils.AccountCheckResult.NOT_VERIFIED -> AccountCheckResult.NotVerified

            MiscUtils.AccountCheckResult.SUCCESS -> AccountCheckResult.Safe
        }
    }

    sealed class AccountDailyPayoutCheckResult {
        class Success(val sameIpDailyAt: Int) : AccountDailyPayoutCheckResult()
        class AlreadyGotTheDailyRewardSameAccount(val tomorrowAtMidnight: Long) : AccountDailyPayoutCheckResult()
        class AlreadyGotTheDailyRewardSameIp(val tomorrowAtMidnight: Long, val detectedIp: String) : AccountDailyPayoutCheckResult()
    }

    sealed class AccountCheckResult {
        object Safe : AccountCheckResult()
        object BlockedIp : AccountCheckResult()
        object BlockedEmail : AccountCheckResult()
        object NotVerified : AccountCheckResult()
    }
}