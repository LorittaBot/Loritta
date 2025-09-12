package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.StoredDivineInterventionSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.io.File
import java.sql.Connection
import java.time.Instant

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

    val boughtStocksAll = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        BoughtStocks.select(BoughtStocks.user, BoughtStocks.price.sum())
            .groupBy(BoughtStocks.user)
            .toList()
    }


    val jobs = boughtStocksAll.chunked(1_000).map { boughtStocks ->
        GlobalScope.launch {
            pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
                var idx = 0
                for (entry in boughtStocks) {
                    val refund = entry[BoughtStocks.price.sum()]!!

                    println("${entry[BoughtStocks.user]} has $refund sonhos in stocks! ($idx/${boughtStocks.size})")

                    val updateCount = Profiles.update({ Profiles.id eq entry[BoughtStocks.user] }) {
                        with(SqlExpressionBuilder) {
                            it.update(money, money + refund)
                        }
                    }

                    if (updateCount != 0) {
                        // Cinnamon transaction system
                        SimpleSonhosTransactionsLogUtils.insert(
                            entry[BoughtStocks.user],
                            Instant.now(),
                            TransactionType.DIVINE_INTERVENTION,
                            refund,
                            StoredDivineInterventionSonhosTransaction(
                                DivineInterventionTransactionEntryAction.ADDED_SONHOS,
                                297153970613387264L,
                                "Removed Broker :("
                            )
                        )
                    } else {
                        println("Skipping update for ${entry[BoughtStocks.user]} because they don't exist in the database!")
                    }
                    idx++
                }
            }
        }
    }

    println("Waiting all tasks to finish...")
    jobs.joinAll()
    println("Done!")
}