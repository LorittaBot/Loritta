package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.imageio.ImageIO

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

    val today = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
    var start = today
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    var end = start
        .plusDays(1)

    val output = StringBuilder()

    val dates = mutableMapOf<LocalDate, Long>()

    val result = pudding.transaction {
        repeat(365 * 5) {
            val count = Dailies.selectAll()
                .where {
                    Dailies.receivedAt greaterEq start.toInstant().toEpochMilli() and (Dailies.receivedAt less end.toInstant().toEpochMilli())
                }
                .count()

            val localDate = start.toLocalDate()

            println("$localDate: $count")

            dates[localDate] = count

            output.append("$localDate: $count")

            if (localDate.year == 2025 && localDate.dayOfMonth == 8 && localDate.monthValue == 7) {
                output.append(" <-- Primeiro dia")
            }

            output.appendLine()

            end = end.minusDays(1)
            start = start.minusDays(1)
        }
    }

    output.appendLine("---")
    output.appendLine("AVERAGES:")
    dates.entries.groupBy { it.key.year }.map { it.key to it.value.map { it.value }.average() }.sortedByDescending { it.first }.withIndex().forEach { (index, it) ->
        output.appendLine("${index + 1}. ${it.first}: ${it.second}")
    }
    output.appendLine("---")
    output.appendLine("TOP DATES:")
    dates.entries.sortedByDescending { it.value }.withIndex().forEach { (index, it) ->
        output.appendLine("${index + 1}. ${it.key}: ${it.value}")
    }

    File("dailies.txt")
        .writeText(output.toString())
}