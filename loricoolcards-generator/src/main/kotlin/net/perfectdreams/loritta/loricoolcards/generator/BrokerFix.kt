package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
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

    pudding.transaction {
        // 10x it!
        val boughtNvidiaStocks = BoughtStocks.selectAll().where {
            BoughtStocks.ticker eq "NVDC34" and (BoughtStocks.boughtAt lessEq 1717787123030)
        }.toList()

        println("Queried everything!")
        println("Batch inserting...")
        var size = 0
        boughtNvidiaStocks.chunked(65_300).forEach {
            println("Current progress: $size")
            for (idx in 1..9) {
                BoughtStocks.batchInsert(it, shouldReturnGeneratedValues = false) {
                    this[BoughtStocks.user] = it[BoughtStocks.user]
                    this[BoughtStocks.boughtAt] = it[BoughtStocks.boughtAt]
                    this[BoughtStocks.ticker] = it[BoughtStocks.ticker]
                    this[BoughtStocks.price] = 0 // zero because it is a split
                }
            }
            size += it.size
        }
    }

    println("Done!")
}