package net.perfectdreams.loritta.morenitta.website.rpc.processors.economy

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.net.Inet6Address
import java.time.ZonedDateTime

object DailyAccountSafetyUtils {
    private val logger = KotlinLogging.logger {}
    private const val EXPANDED_IPV6_HOUSEHOLD_PREFIX = 4

    suspend fun checkIfUserCanPayout(loritta: LorittaBot, userIdentification: TemmieDiscordAuth.UserIdentification, ip: String): AccountDailyPayoutCheckResult {
        val isIPv6 = ip.contains(":")

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

        if (lastReceivedDailyAt.isNotEmpty()) {
            logger.info { "Blocking ${userIdentification.id.toLong()} because they already received the daily reward today" }
            if (!loritta.isOwner(userIdentification.id.toLong())) {
                return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameAccount(tomorrowAtMidnight)
            }
        }

        // Depending on if the user is using IPv6 or IPv4, we do different checks
        if (isIPv6) {
            // For IPv6, we attempt to extract the "prefix" of the IPv6
            // While we don't actually know the prefix, it seems that most providers use /64 for residential usage
            // And it seems to be pretty dead on, rarely you'll have someone with the same IPv6 prefix if they don't live in the same house/using same network
            val expandedSelfIp = expandIPv6Address(ip)
            val selfIpHouseholdPrefixBlocks = expandedSelfIp.split(":").take(EXPANDED_IPV6_HOUSEHOLD_PREFIX)

            // This SUCKS A LOT BUT THERE IS NOT A BETTER SOLUTION FOR THIS
            // The best...est solution would be to save the IPv6s in a binary format (maybe) or in PostgreSQL's inet format (also maybe)
            // But because we are already saving them using TEXT, we need to query EVERYTHING to then expand and check
            val allDailiesThatWereReceivedToday = loritta.newSuspendedTransaction {
                Dailies.select(Dailies.ip)
                    .where { Dailies.receivedAt greaterEq todayAtMidnight }
                    .orderBy(Dailies.receivedAt, SortOrder.DESC)
                    .map {
                        it[Dailies.ip]
                    }
            }

            // TODO: Should we also implement the "sameIpDailyOneHourAgoAt" IPv4 checks too?

            var trippedSamePrefixChecks = 0
            val checkedDailies = mutableListOf<String>()

            for (dailyIp in allDailiesThatWereReceivedToday) {
                val expandedDailyIp = expandIPv6Address(dailyIp)

                val dailyIpHouseholdPrefixBlocks = expandedDailyIp.split(":").take(EXPANDED_IPV6_HOUSEHOLD_PREFIX)

                if (dailyIpHouseholdPrefixBlocks == selfIpHouseholdPrefixBlocks) {
                    // Just like IPv4 checks, we will do some checks here too
                    if (expandedDailyIp == expandedSelfIp) {
                        logger.info { "Blocking ${userIdentification.id.toLong()} because they already received the daily reward today (Same IPv6)" }
                        if (!loritta.isOwner(userIdentification.id.toLong())) {
                            return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp(
                                tomorrowAtMidnight,
                                ip
                            )
                        }
                    }

                    trippedSamePrefixChecks++

                    if (trippedSamePrefixChecks == 3) {
                        logger.info { "Blocking ${userIdentification.id.toLong()} because they already received the daily reward today (IPv6 - Too many rewards on the same IPv6 prefix - Household prefix is $dailyIpHouseholdPrefixBlocks)" }
                        if (!loritta.isOwner(userIdentification.id.toLong())) {
                            return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp(
                                tomorrowAtMidnight,
                                ip
                            )
                        }
                    }
                }

                checkedDailies.add(expandedDailyIp)
            }

            return AccountDailyPayoutCheckResult.Success(trippedSamePrefixChecks)
        } else {
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
                logger.info { "Blocking ${userIdentification.id.toLong()} because they already received the daily reward today (Same IPv4)" }
                if (!loritta.isOwner(userIdentification.id.toLong())) {
                    return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameAccount(tomorrowAtMidnight)
                }
            }

            if (sameIpDailyAt.isNotEmpty()) {
                // If there's more than 3 users getting daily on the same IPv4, then block them!
                if (sameIpDailyAt.size >= 3) {
                    logger.info { "Blocking ${userIdentification.id.toLong()} because they already received the daily reward today (IPv4 - Too many rewards on the same IP)" }
                    return AccountDailyPayoutCheckResult.AlreadyGotTheDailyRewardSameIp(
                        tomorrowAtMidnight,
                        ip
                    )
                }
            }

            return AccountDailyPayoutCheckResult.Success(sameIpDailyAt.size)
        }
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

    /**
     * Expands the provided [ipv6]
     *
     * @param ipv6 the IPv6 that will be expanded
     * @return the IPv6 address in expanded format
     */
    // Thanks Deepseek
    private fun expandIPv6Address(ipv6: String): String {
        // Split the address into two parts if it contains "::"
        val parts = ipv6.split("::")
        val left = parts[0].split(":").filter { it.isNotEmpty() }
        val right = if (parts.size > 1) parts[1].split(":").filter { it.isNotEmpty() } else emptyList()

        // Calculate the number of missing hextets
        val missingHextets = 8 - (left.size + right.size)

        // Create a list of all hextets, filling in the missing ones with "0000"
        val allHextets = left + List(missingHextets) { "0000" } + right

        // Pad each hextet to 4 characters with leading zeros
        val expandedHextets = allHextets.map { it.padStart(4, '0') }

        // Join the hextets with colons to form the expanded IPv6 address
        return expandedHextets.joinToString(":")
    }

    private fun extractIPv6HouseholdPrefix(ipv6Address: String): List<String> {
        // Extracting the IPv6 blocks is HARD due to compression
        val address = ipv6Address.replace("::", ":0000:0000:")
        return address.split(":")
    }

    /**
     * Gets the prefix of [ipv6Address], using [prefixLength] as the length
     *
     * The result is a IPv6 in compressed format
     *
     * @param ipv6Address the IPv6 address
     * @param prefixLength the prefix length
     * @return the compressed IPv6 address
     */
    // Thanks Deepseek
    private fun getCompressedIPv6Prefix(ipv6Address: String, prefixLength: Int): String {
        // Parse the IPv6 address
        val inet6Address = Inet6Address.getByName(ipv6Address) as Inet6Address

        // Get the bytes of the IPv6 address
        val addressBytes = inet6Address.address

        // Calculate the number of bytes in the prefix
        val prefixBytes = prefixLength / 8

        // Create a new byte array for the prefix
        val prefix = ByteArray(16) { 0 }

        // Copy the prefix bytes
        System.arraycopy(addressBytes, 0, prefix, 0, prefixBytes)

        // Create a new Inet6Address with the prefix
        val prefixAddress = Inet6Address.getByAddress(null, prefix) as Inet6Address

        // Get the compressed IPv6 address
        return prefixAddress.hostAddress.replace(Regex("(:0)+"), ":").replace(Regex("^0+"), "")
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