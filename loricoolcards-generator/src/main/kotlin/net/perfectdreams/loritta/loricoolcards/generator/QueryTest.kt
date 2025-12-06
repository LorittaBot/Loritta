package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import javax.imageio.ImageIO

suspend fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

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

    val result = pudding.transaction {
        SimpleSonhosTransactionsLog.selectAll()
            .where {
                SimpleSonhosTransactionsLog.type inList listOf(TransactionType.PAYMENT) and (SimpleSonhosTransactionsLog.sonhos eq 100_000_000)
            }
            .orderBy(SimpleSonhosTransactionsLog.timestamp, SortOrder.DESC)
            .limit(100_000)
            .offset(0)
            .map {
                when (val stored = Json.decodeFromString<StoredSonhosTransaction>(it[SimpleSonhosTransactionsLog.metadata])) {
                    is StoredShipEffectSonhosTransaction -> ShipEffectSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos]
                    )

                    is StoredDailyRewardSonhosTransaction -> DailyRewardSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos]
                    )

                    is StoredBotVoteSonhosTransaction -> BotVoteSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        stored.websiteSource,
                        it[SimpleSonhosTransactionsLog.sonhos]
                    )

                    is StoredDivineInterventionSonhosTransaction -> DivineInterventionSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        stored.action,
                        UserId(stored.editedBy),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.reason
                    )

                    is StoredDailyTaxSonhosTransaction -> DailyTaxSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.maxDayThreshold,
                        stored.minimumSonhosForTrigger
                    )

                    is StoredPaymentSonhosTransaction -> PaymentSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        UserId(stored.givenBy),
                        UserId(stored.receivedBy),
                        it[SimpleSonhosTransactionsLog.sonhos],
                    )

                    is StoredBrokerSonhosTransaction -> BrokerSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        stored.action,
                        stored.ticker,
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.stockPrice,
                        stored.stockQuantity
                    )

                    is StoredRaffleRewardTransaction -> {
                        val raffle = Raffles.selectAll().where {
                            Raffles.id eq stored.raffleId
                        }.first()

                        RaffleRewardSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            raffle[Raffles.paidOutPrize] ?: -1,
                            raffle[Raffles.paidOutPrizeAfterTax] ?: raffle[Raffles.paidOutPrize] ?: -1,
                            raffle[Raffles.tax],
                            raffle[Raffles.taxPercentage]
                        )
                    }

                    is StoredRaffleTicketsTransaction -> RaffleTicketsSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.ticketQuantity
                    )

                    is StoredSonhosBundlePurchaseTransaction -> SonhosBundlePurchaseSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos]
                    )

                    is StoredCoinFlipBetTransaction -> {
                        val matchmakingResult = CoinFlipBetMatchmakingResults.selectAll().where {
                            CoinFlipBetMatchmakingResults.id eq stored.matchmakingResultId
                        }.first()

                        CoinFlipBetSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(matchmakingResult[CoinFlipBetMatchmakingResults.winner].value),
                            UserId(matchmakingResult[CoinFlipBetMatchmakingResults.loser].value),
                            matchmakingResult[CoinFlipBetMatchmakingResults.quantity],
                            matchmakingResult[CoinFlipBetMatchmakingResults.quantityAfterTax],
                            matchmakingResult[CoinFlipBetMatchmakingResults.tax],
                            matchmakingResult[CoinFlipBetMatchmakingResults.taxPercentage]
                        )
                    }

                    is StoredCoinFlipBetGlobalTransaction -> {
                        val matchmakingResult = CoinFlipBetGlobalMatchmakingResults.selectAll().where {
                            CoinFlipBetGlobalMatchmakingResults.id eq stored.matchmakingResultId
                        }.first()

                        CoinFlipBetGlobalSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.winner].value),
                            UserId(matchmakingResult[CoinFlipBetGlobalMatchmakingResults.loser].value),
                            matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantity],
                            matchmakingResult[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax],
                            matchmakingResult[CoinFlipBetGlobalMatchmakingResults.tax],
                            matchmakingResult[CoinFlipBetGlobalMatchmakingResults.taxPercentage],
                            matchmakingResult[CoinFlipBetGlobalMatchmakingResults.timeOnQueue].toMillis()
                        )
                    }

                    is StoredSparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        stored.action,
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.sparklyPowerSonhos,
                        stored.playerName,
                        stored.playerUniqueId,
                        stored.exchangeRate
                    )

                    is StoredChristmas2022SonhosTransaction -> Christmas2022SonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.gifts
                    )

                    is StoredEaster2023SonhosTransaction -> Easter2023SonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.baskets
                    )

                    is StoredPowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.liveId,
                        stored.streamId
                    )

                    is StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.liveId,
                        stored.streamId
                    )

                    is StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.eventId
                    )

                    is StoredLoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        it[SimpleSonhosTransactionsLog.sonhos],
                        stored.eventId
                    )

                    is StoredLoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransaction(
                        it[SimpleSonhosTransactionsLog.id].value,
                        it[SimpleSonhosTransactionsLog.type],
                        it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                        UserId(it[SimpleSonhosTransactionsLog.user].value),
                        UserId(stored.givenBy),
                        UserId(stored.receivedBy),
                        it[SimpleSonhosTransactionsLog.sonhos],
                    )

                    is StoredEmojiFightBetSonhosTransaction -> {
                        val emojiFightMatchmakingResults = EmojiFightMatchmakingResults.selectAll().where {
                            EmojiFightMatchmakingResults.id eq stored.emojiFightMatchmakingResultsId
                        }.first()

                        val winnerInMatch = EmojiFightParticipants.selectAll().where { EmojiFightParticipants.id eq emojiFightMatchmakingResults[EmojiFightMatchmakingResults.winner] }
                            .first()

                        val usersInMatch = EmojiFightParticipants.selectAll().where { EmojiFightParticipants.match eq winnerInMatch[EmojiFightParticipants.match] }
                            .count()

                        TODO()
                        /* EmojiFightBetSonhosTransaction(
                            it[SimpleSonhosTransactionsLog.id].value,
                            it[SimpleSonhosTransactionsLog.type],
                            it[SimpleSonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SimpleSonhosTransactionsLog.user].value),
                            UserId(winnerInMatch[EmojiFightParticipants.user].value),
                            usersInMatch,
                            winnerInMatch[EmojiFightParticipants.emoji],
                            emojiFightMatchmakingResults[EmojiFightMatchmakingResults.entryPrice],
                            emojiFightMatchmakingResults[EmojiFightMatchmakingResults.entryPriceAfterTax],
                            emojiFightMatchmakingResults[EmojiFightMatchmakingResults.tax],
                            emojiFightMatchmakingResults[EmojiFightMatchmakingResults.taxPercentage]
                        ) */
                    }

                    StoredBomDiaECiaCallCalledTransaction -> TODO()
                    StoredBomDiaECiaCallWonTransaction -> TODO()
                    is StoredGarticosTransferTransaction -> TODO()
                    is StoredLorittaItemShopBoughtBackgroundTransaction -> TODO()
                    is StoredLorittaItemShopBoughtProfileDesignTransaction -> TODO()
                    is StoredReactionEventSonhosTransaction -> TODO()
                    is StoredChargebackedSonhosBundleTransaction -> TODO()
                    is StoredLorittaItemShopComissionBackgroundTransaction -> TODO()
                    is StoredLorittaItemShopComissionProfileDesignTransaction -> TODO()
                    is StoredMarriageMarryTransaction -> TODO()
                    is StoredAPIInitiatedPaymentSonhosTransaction -> TODO()
                    is StoredThirdPartyPaymentSonhosTransaction -> TODO()
                    StoredVacationModeLeaveTransaction -> TODO()
                    StoredMarriageLoveLetterTransaction -> TODO()
                    StoredMarriageRestoreTransaction -> TODO()
                    StoredMarriageRestoreAutomaticTransaction -> TODO()
                    is StoredReputationDeletedTransaction -> TODO()
                    is StoredBlackjackDoubleDownTransaction -> TODO()
                    is StoredBlackjackInsurancePayoutTransaction -> TODO()
                    is StoredBlackjackInsuranceTransaction -> TODO()
                    is StoredBlackjackJoinedTransaction -> TODO()
                    is StoredBlackjackPayoutTransaction -> TODO()
                    is StoredBlackjackRefundTransaction -> TODO()
                    is StoredBlackjackSplitTransaction -> TODO()
                    is StoredBlackjackTiedTransaction -> TODO()
                    is StoredDropChatTransaction -> TODO()
                }
            }
    }

    File("transactions_stuff3.json")
        .writeText(
            Json {
                prettyPrint = true
            }.encodeToString(result)
        )

    println("Finished!")
}