package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import javax.imageio.ImageIO

suspend fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

    val userId = 990358659374673940
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
        ExecutedApplicationCommandsLog.selectAll()
            .where {
                ExecutedApplicationCommandsLog.userId eq userId
            }
            .orderBy(ExecutedApplicationCommandsLog.sentAt, SortOrder.DESC)
            .map {
                val fields = it.fieldIndex

                println(it)
                buildJsonObject {
                    for ((expr, i) in fields) {
                        put(expr.toString().substringAfterLast("."), it.get(expr)?.toString())
                    }
                }
            }
    }

    File("commands_$userId.json")
        .writeText(
            Json {
                prettyPrint = true
            }.encodeToString(result)
        )

    println("Finished!")
}