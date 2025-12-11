package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.sum
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
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    val map = pudding.transaction {
        val dateField = EmojiFightMatches.finishedAt.date()
        val taxSumField = EmojiFightMatchmakingResults.tax.sum()

        EmojiFightMatchmakingResults.innerJoin(EmojiFightMatches)
            .select(dateField, taxSumField)
            .groupBy(dateField)
            .orderBy(dateField, SortOrder.DESC)
            .associate {
                it[dateField] to it[taxSumField]
            }
    }

    for ((date, taxTotal) in map) {
        println("$date - $taxTotal")
    }

    println("Done!")
}