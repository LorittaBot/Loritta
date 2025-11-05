package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.StoredDivineInterventionSonhosTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.io.File
import java.sql.Connection
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

    val inst = OffsetDateTime.of(2025, 11, 1, 23, 0, 0, 0, ZoneOffset.UTC)
        .toInstant()

    val affected = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        SimpleSonhosTransactionsLog
            .selectAll()
            .where {
                SimpleSonhosTransactionsLog.type eq TransactionType.INACTIVE_DAILY_TAX and (SimpleSonhosTransactionsLog.timestamp greaterEq inst)
            }
            .toList()
    }

    val paidOut = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        SimpleSonhosTransactionsLog
            .selectAll()
            .where {
                SimpleSonhosTransactionsLog.type eq TransactionType.DIVINE_INTERVENTION and (SimpleSonhosTransactionsLog.timestamp greaterEq inst)
            }
            .toList()
            .map {
                it[SimpleSonhosTransactionsLog.user].value
            }
            .toSet()
    }

    println("Paid out: ${paidOut.size}")

    val affectedAndNotProcessed = affected.filter { it[SimpleSonhosTransactionsLog.user].value !in paidOut }
    var cIndex = 0
    val semaphore = Semaphore(4)

    val job = affectedAndNotProcessed
        .chunked(100)
        .map { query ->
            var rcIndex = cIndex
            println("Processing chunk ${cIndex++}")

            GlobalScope.async {
                semaphore.withPermit {
                    var index = 0

                    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
                        for (entry in query) {
                            println("(${index}/${query.size} - chunk ${rcIndex}) Paying ${entry[SimpleSonhosTransactionsLog.user]} ${entry[SimpleSonhosTransactionsLog.sonhos]} sonhos back")

                            Profiles.update({ Profiles.id eq entry[SimpleSonhosTransactionsLog.user] }) {
                                with(SqlExpressionBuilder) {
                                    it.update(money, money + entry[SimpleSonhosTransactionsLog.sonhos])
                                }
                            }

                            // Cinnamon transaction system
                            SimpleSonhosTransactionsLogUtils.insert(
                                entry[SimpleSonhosTransactionsLog.user].value,
                                Instant.now(),
                                TransactionType.DIVINE_INTERVENTION,
                                entry[SimpleSonhosTransactionsLog.sonhos],
                                StoredDivineInterventionSonhosTransaction(
                                    DivineInterventionTransactionEntryAction.ADDED_SONHOS,
                                    123170274651668480L,
                                    "Buggy DM Notification"
                                )
                            )
                            index++
                        }
                    }
                }
            }
        }

    job.awaitAll()

    println("Done!")
}