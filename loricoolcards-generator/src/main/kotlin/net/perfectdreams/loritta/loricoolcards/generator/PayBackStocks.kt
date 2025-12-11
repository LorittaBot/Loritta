package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.StoredDivineInterventionSonhosTransaction
import net.perfectdreams.loritta.serializable.StoredSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    val tickerId = "GOLL4"
    val payoutPrice = (0.79 * 100).toLong()

    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        val countField = BoughtStocks.ticker.count()
        val query = BoughtStocks.select(BoughtStocks.user, countField)
            .where {
                BoughtStocks.ticker eq tickerId
            }
            .groupBy(BoughtStocks.user)
            .toList()

        // This SUCKS but it is what it is
        // We do this like this to speed up the task
        val grouped = query.groupBy { it[countField] }

        var totalProcessed = 0
        for (entry in grouped) {
            val giveOutSonhos = entry.key * payoutPrice

            println("[$totalProcessed/${query.size}] Processing ${entry.value.size} users... (${entry.key} entries) ($giveOutSonhos sonhos)")
            for (entry in entry.value) {
                println("User: ${entry[BoughtStocks.user]}")
            }

            Profiles.update({ Profiles.id inList entry.value.map { it[BoughtStocks.user] }}) {
                it[Profiles.money] = Profiles.money + giveOutSonhos
            }

            SimpleSonhosTransactionsLog.batchInsert(entry.value, shouldReturnGeneratedValues = false) {
                this[SimpleSonhosTransactionsLog.user] = it[BoughtStocks.user]
                this[SimpleSonhosTransactionsLog.timestamp] = Instant.now()
                this[SimpleSonhosTransactionsLog.type] = TransactionType.DIVINE_INTERVENTION
                this[SimpleSonhosTransactionsLog.sonhos] = giveOutSonhos
                this[SimpleSonhosTransactionsLog.metadata] = Json.encodeToString<StoredSonhosTransaction>(
                    StoredDivineInterventionSonhosTransaction(
                        DivineInterventionTransactionEntryAction.ADDED_SONHOS,
                        297153970613387264,
                        "$tickerId removed from the broker"
                    )
                )
            }

            totalProcessed += entry.value.size
        }

        /* BoughtStocks.deleteWhere {
            BoughtStocks.ticker eq tickerId
        } */
    }

    println("Done!")
}