package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import net.perfectdreams.loritta.serializable.StoredDivineInterventionSonhosTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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

    // mergeTarget -> 1 stock
    val mergeTarget = 100
    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        val sumField = BoughtStocks.ticker.count()
        val boughtStocks = BoughtStocks.select(BoughtStocks.user, sumField).where {
            BoughtStocks.ticker eq "AMER3" and (BoughtStocks.boughtAt lessEq 1724760000000)
        }.groupBy(BoughtStocks.user).toList()

        println("Queried everything!")
        // Because we are merging multiple stocks, it is a *bit* hard
        var idx = 0
        for (boughtStocksInfo in boughtStocks) {
            println("Current progress: $idx/${boughtStocks.size}")
            val userThatBoughtTheStock = boughtStocksInfo[BoughtStocks.user]
            val howManyStocksTheyHave = boughtStocksInfo[sumField]

            val newStockCount = howManyStocksTheyHave / mergeTarget
            val remainderStocks = howManyStocksTheyHave % mergeTarget
            val howManyTheyHaveAfterRemainderRemoved = howManyStocksTheyHave - remainderStocks

            println("User $userThatBoughtTheStock that has $howManyStocksTheyHave stocks will now have $newStockCount (remainder: $remainderStocks / remainder removed: $howManyTheyHaveAfterRemainderRemoved)")

            // Let's take care about the remainderStocks first, for us to work on a "clean" slate
            if (remainderStocks != 0L) {
                println("Deleting $remainderStocks from $userThatBoughtTheStock (remainder)")
                val stocksToBeDeleted = BoughtStocks.selectAll()
                    .where {
                        BoughtStocks.ticker eq "AMER3" and (BoughtStocks.boughtAt lessEq 1724760000000) and (BoughtStocks.user eq userThatBoughtTheStock)
                    }.limit(remainderStocks.toInt())
                    .orderBy(BoughtStocks.boughtAt, SortOrder.DESC)
                    .toList()

                if (stocksToBeDeleted.size.toLong() != remainderStocks)
                    error("Incorrect deleted rows (remainder) for ${userThatBoughtTheStock}! ${stocksToBeDeleted.size} != $remainderStocks")


                // Pay back the users
                var refund = 0L
                for (stock in stocksToBeDeleted) {
                    refund += stock[BoughtStocks.price]
                }

                println("User $userThatBoughtTheStock will be refunded $refund!")

                Profiles.update({ Profiles.id eq userThatBoughtTheStock }) {
                    with(SqlExpressionBuilder) {
                        it.update(money, money + refund)
                    }
                }

                // Cinnamon transaction system
                SimpleSonhosTransactionsLogUtils.insert(
                    userThatBoughtTheStock,
                    Instant.now(),
                    TransactionType.DIVINE_INTERVENTION,
                    refund,
                    StoredDivineInterventionSonhosTransaction(
                        DivineInterventionTransactionEntryAction.ADDED_SONHOS,
                        297153970613387264L,
                        "Grouping of AMER3 27/08/2024"
                    )
                )

                // Delete the remainder stocks!
                BoughtStocks.deleteWhere {
                    BoughtStocks.id inList stocksToBeDeleted.map { it[BoughtStocks.id] }
                }
            }

            if (howManyTheyHaveAfterRemainderRemoved != 0L) {
                // The user has enough stocks for us to delete some of their old ones and regroup them!
                // The limit should be...
                // howManyStocksTheyHave - (howManyStocksTheyHave - newStockCount)

                val amountOfStocksToDelete = howManyTheyHaveAfterRemainderRemoved - newStockCount
                println("Deleting $amountOfStocksToDelete stocks from $userThatBoughtTheStock... (real)")

                val stocksToBeRemoved = BoughtStocks.selectAll().where {
                    BoughtStocks.ticker eq "AMER3" and (BoughtStocks.boughtAt lessEq 1724760000000) and (BoughtStocks.user eq userThatBoughtTheStock)
                }
                    .limit(amountOfStocksToDelete.toInt())
                    .orderBy(BoughtStocks.boughtAt, SortOrder.DESC)
                    .toList()
                    .map { it[BoughtStocks.id] }

                var totalDeletedRows = 0
                stocksToBeRemoved.chunked(65_535).forEach { chunk ->
                    val deletedRows = BoughtStocks.deleteWhere { BoughtStocks.id inList chunk }
                    totalDeletedRows += deletedRows
                }

                if (totalDeletedRows.toLong() != amountOfStocksToDelete)
                    error("Incorrect deleted rows (real) for ${userThatBoughtTheStock}! $totalDeletedRows != $amountOfStocksToDelete")

                // And now we update the remainder to match the new value!
                val updt = BoughtStocks.update({ BoughtStocks.ticker eq "AMER3" and (BoughtStocks.user eq userThatBoughtTheStock) }) {
                    with(SqlExpressionBuilder) {
                        it.update(price, price * mergeTarget.toLong())
                    }
                }

                println("Updated the value of $updt stocks for $userThatBoughtTheStock! (real)")

            }
            idx++
        }
    }

    println("Done!")
}