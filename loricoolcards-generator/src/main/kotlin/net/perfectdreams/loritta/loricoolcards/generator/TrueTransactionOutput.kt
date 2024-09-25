package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.*
import org.jetbrains.exposed.sql.selectAll
import java.io.File

suspend fun main() {
    val configurationFile = File(System.getProperty("conf") ?: "./loricoolcards-production-stickers-generator.conf")

    if (!configurationFile.exists()) {
        println("Missing configuration file!")
        System.exit(1)
        return
    }

    val config = readConfigurationFromFile<LoriCoolCardsGeneratorProductionStickersConfig>(configurationFile)

    val pudding = Pudding.createPostgreSQLPudding(
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    val storedUsers = mutableMapOf<Long?, Long>()

    val userId = 236167700777271297L
    val map = pudding.transaction {
        val result = pudding.sonhos
            .getUserTransactions(
                UserId(236167700777271297L),
                TransactionType.entries,
                Int.MAX_VALUE,
                0,
                null,
                null
            )


        val emojiFightMatchmakingResultsUsersInMatches = EmojiFightParticipants.selectAll()
            .where {
                EmojiFightParticipants.match inList result.filterIsInstance<EmojiFightBetSonhosTransaction>()
                    .mapNotNull { it.matchId }
            }
            .toList()

        result.forEach {
            when (it) {
                is CoinFlipBetGlobalSonhosTransaction -> {
                    if (it.winner.value.toLong() != userId) {
                        storedUsers[it.winner.value.toLong()] = storedUsers.getOrDefault(userId, 0L) - it.quantityAfterTax
                    } else {
                        storedUsers[it.winner.value.toLong()] = storedUsers.getOrDefault(userId, 0L) + it.quantityAfterTax
                    }
                }
                is CoinFlipBetSonhosTransaction -> {
                    if (it.winner.value.toLong() != userId) {
                        storedUsers[it.winner.value.toLong()] = storedUsers.getOrDefault(userId, 0L) - it.quantityAfterTax
                    } else {
                        storedUsers[it.winner.value.toLong()] = storedUsers.getOrDefault(userId, 0L) + it.quantityAfterTax
                    }
                }
                // is DailyRewardSonhosTransaction -> TODO()
                // is DailyTaxSonhosTransaction -> TODO()
                // is DivineInterventionSonhosTransaction -> TODO()
                // is Easter2023SonhosTransaction -> TODO()
                is EmojiFightBetSonhosTransaction -> {
                    val participants = emojiFightMatchmakingResultsUsersInMatches.filter { rr ->
                        rr[EmojiFightParticipants.match].value == it.matchId
                    }

                    for (participant in participants) {
                        if (participant[EmojiFightParticipants.user].value == it.winner.value.toLong()) {
                            storedUsers[participant[EmojiFightParticipants.user].value] = storedUsers.getOrDefault(userId, 0L) + (it.entryPriceAfterTax * (it.usersInMatch - 1))
                        } else {
                            storedUsers[participant[EmojiFightParticipants.user].value] = storedUsers.getOrDefault(userId, 0L) - it.entryPriceAfterTax
                        }
                    }
                }

                // is LoriCoolCardsBoughtBoosterPackSonhosTransaction -> TODO()
                // is LoriCoolCardsFinishedAlbumSonhosTransaction -> TODO()
                // is LoriCoolCardsPaymentSonhosTradeTransaction -> TODO()
                is PaymentSonhosTransaction -> {
                    storedUsers[it.receivedBy.value.toLong()] = storedUsers.getOrDefault(it.receivedBy.value.toLong(), 0L) + (it.sonhos)
                    storedUsers[it.givenBy.value.toLong()] = storedUsers.getOrDefault(it.givenBy.value.toLong(), 0L) - (it.sonhos)
                }
                // is PowerStreamClaimedFirstSonhosRewardSonhosTransaction -> TODO()
                // is PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> TODO()
                // is RaffleRewardSonhosTransaction -> TODO()
                // is RaffleTicketsSonhosTransaction -> TODO()
                // is ShipEffectSonhosTransaction -> TODO()
                // is SonhosBundlePurchaseSonhosTransaction -> TODO()
                // is SparklyPowerLSXSonhosTransaction -> TODO()
                is UnknownSonhosTransaction -> {
                    println("Unknown!")
                }
                else -> {
                    println("Not handled")
                }
            }
        }
    }

    storedUsers.entries

        .sortedByDescending { it.value }
        .forEach {
            println("${it.key}: ${it.value}")
        }
    println("Done!")
}