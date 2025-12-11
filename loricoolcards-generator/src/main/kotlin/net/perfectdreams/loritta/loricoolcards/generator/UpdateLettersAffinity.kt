package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageLoveLetters
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriagesOld
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.io.File
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

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

    pudding.transaction(transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ) {
        val loveLetters = MarriageLoveLetters.selectAll()
            .toList()

        val users = mutableSetOf<Entry>()

        for (letter in loveLetters) {
            val entry = Entry(
                letter[MarriageLoveLetters.sentBy],
                letter[MarriageLoveLetters.sentAt].atZone(Constants.LORITTA_TIMEZONE).toLocalDate(),
            )

            if (!users.contains(entry)) {
                println("$entry")
                UserMarriages.update({ UserMarriages.id eq letter[MarriageLoveLetters.marriage] }) {
                    it[UserMarriages.affinity] = UserMarriages.affinity + 2
                }
            } else {
                println("Skipping $entry...")
            }

            users.add(
                Entry(
                    letter[MarriageLoveLetters.sentBy],
                    letter[MarriageLoveLetters.sentAt].atZone(Constants.LORITTA_TIMEZONE).toLocalDate(),
                )
            )
        }
    }

    println("Done!")
}

data class Entry(
    val userId: Long,
    val localDate: LocalDate
)