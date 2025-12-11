package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
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
        LorittaBot.SCHEMA_VERSION,
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    var lastId: Long? = null

    while (true) {
        pudding.transaction {
            val _lastId = lastId
            if (_lastId == null) {
                lastId = ExecutedApplicationCommandsLog.selectAll()
                    .limit(1)
                    .orderBy(ExecutedApplicationCommandsLog.id, SortOrder.DESC)
                    .first()[ExecutedApplicationCommandsLog.id].value
                return@transaction
            }

            val r = ExecutedApplicationCommandsLog.selectAll()
                .where {
                    ExecutedApplicationCommandsLog.id greater  _lastId
                }
                .orderBy(ExecutedApplicationCommandsLog.sentAt, SortOrder.DESC)
                .toList()

            r.forEach {
                println("${it[ExecutedApplicationCommandsLog.sentAt]} ${it[ExecutedApplicationCommandsLog.executor]}")
            }

            if (r.isNotEmpty()) {
                lastId = r[0][ExecutedApplicationCommandsLog.id].value
            }
        }
        println("Finished querying")
    }
}