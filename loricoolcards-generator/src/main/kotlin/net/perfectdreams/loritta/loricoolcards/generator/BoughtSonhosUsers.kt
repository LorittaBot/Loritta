package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedDiscordUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
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
        config.pudding.address,
        config.pudding.database,
        config.pudding.username,
        config.pudding.password
    )

    pudding.transaction {
        val payments = Payments
            .select(Payments.userId, Payments.money.sum())
            .where {
                Payments.paidAt greaterEq 1726240088000 and (Payments.reason eq PaymentReason.SONHOS_BUNDLE)
            }
            .groupBy(Payments.userId)
            .orderBy(Payments.money.sum(), SortOrder.DESC)

        val cachedUsers = CachedDiscordUsers.selectAll().where { CachedDiscordUsers.id inList payments.map { it[Payments.userId] } }
            .toList()

        val userToFancy = mutableMapOf<Long, String>()

        for (cachedUser in cachedUsers) {
            userToFancy[cachedUser[CachedDiscordUsers.id].value] = "${cachedUser[CachedDiscordUsers.name]} [${cachedUser[CachedDiscordUsers.globalName]}] (${cachedUser[CachedDiscordUsers.id]})"
        }

        val largestName = userToFancy.values.maxOf { it.length }

        payments.forEach {
            val fancyName = userToFancy[it[Payments.userId]]

            println("${fancyName}: ${it[Payments.money.sum()]}")
        }
    }

    println("Done!")
}