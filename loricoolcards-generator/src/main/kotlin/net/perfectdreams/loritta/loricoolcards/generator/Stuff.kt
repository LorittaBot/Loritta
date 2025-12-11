package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )


    var startStuff = LocalDateTime.of(2022, 2, 1, 0, 0)
        .atZone(ZoneId.of("America/Sao_Paulo"))

    while (startStuff.year != 2025) {
        pudding.transaction {
            val winnerCount = CoinFlipBetMatchmakingResults.winner.count()
            val loserCount = CoinFlipBetMatchmakingResults.loser.count()

            val startInstant = startStuff.toInstant()

            val endStuff = startStuff.plusMonths(1)
            val endInstant = endStuff.toInstant()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val result1 = CoinFlipBetMatchmakingResults.select(CoinFlipBetMatchmakingResults.winner, winnerCount)
                .where {
                    CoinFlipBetMatchmakingResults.timestamp greaterEq startInstant and (CoinFlipBetMatchmakingResults.timestamp lessEq endInstant)
                }
                .groupBy(CoinFlipBetMatchmakingResults.winner)
                .having { winnerCount greaterEq 10 }
                .toList()
                .map { it[CoinFlipBetMatchmakingResults.winner].value }

            val result2 = CoinFlipBetMatchmakingResults.select(CoinFlipBetMatchmakingResults.loser, loserCount)
                .where {
                    CoinFlipBetMatchmakingResults.timestamp greaterEq startInstant and (CoinFlipBetMatchmakingResults.timestamp lessEq endInstant)
                }
                .groupBy(CoinFlipBetMatchmakingResults.loser)
                .having { loserCount greaterEq 10 }
                .toList()
                .map { it[CoinFlipBetMatchmakingResults.loser].value }

            val rFinal = result1.intersect(result2).toList()
            println("${formatter.format(startInstant.atZone(ZoneId.of("America/Sao_Paulo")))} - ${rFinal.size}")

            startStuff = startStuff.plusMonths(1)
        }
    }
}