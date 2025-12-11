package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.batchInsert
import java.io.File
import java.sql.Connection

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

    val map = mapOf(
        Pair(1163747014220201999, 100000 / 100),
        Pair(1190788622127988767, 100000 / 100),
        Pair(480171509726314497, 97310 / 100),
        Pair(617442557550395392, 10200 / 100),
        Pair(676103461447794688, 18100 / 100),
        Pair(1238695101467856959, 6000 / 100),
        Pair(1238695101467856959, 16000 / 100),
        Pair(1155718788864544818, 100000 / 100),
        Pair(1135677583967662090, 100000 / 100),
        Pair(535496425908338698, 100000 / 100),
        Pair(1065057056475861124, 100000 / 100),
        Pair(1083470343731159070, 100000 / 100),
        Pair(1258467434227302592, 100000 / 100),
        Pair(438487261055680512, 18640 / 100),
        Pair(647457149718626346, 100000 / 100),
        Pair(1061823417579474974, 99100 / 100)
    )

    map.forEach {
        println(it)
    }

    val now = System.currentTimeMillis()
    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
        map.forEach { (userId, quantity) ->
            BoughtStocks.batchInsert(0 until quantity, shouldReturnGeneratedValues = false) {
                this[BoughtStocks.user] = userId
                this[BoughtStocks.ticker] = "AMER3"
                this[BoughtStocks.price] = 0
                this[BoughtStocks.boughtAt] = now
            }
        }
    }

    println("Done!")
}