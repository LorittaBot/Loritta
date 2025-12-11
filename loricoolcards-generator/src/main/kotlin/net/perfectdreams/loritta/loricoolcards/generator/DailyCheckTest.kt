package net.perfectdreams.loritta.loricoolcards.generator

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService.Companion.validBannedUsersList
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrowserFingerprints
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.sql.Connection
import java.time.Instant

suspend fun main() {
    val http = HttpClient {}

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



    pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
        val now = Instant.now()
        val nowInThePast = now.minusSeconds(604_800) // 7 days

        val dailiesRecentlyRetrievedHours = Dailies
            .innerJoin(BrowserFingerprints)
            .innerJoin(Profiles, { Profiles.id }, { Dailies.receivedById })
            .selectAll()
            .where {
                Dailies.receivedAt greaterEq nowInThePast.toEpochMilli() and (Dailies.receivedById notInSubQuery validBannedUsersList(now.toEpochMilli()))
            }
            .toList()

        val allClientIds = dailiesRecentlyRetrievedHours.map { it[BrowserFingerprints.clientId] }
        val alreadyChecked = mutableSetOf<Long>()

        // We can't actually chunk by 65_535 because it is ALL PARAMETERS of the query (every client ID + the subquery)
        val clientIdsThatAreBanned = allClientIds.chunked(65_530).flatMap { clientIds ->
            Dailies
                .innerJoin(BrowserFingerprints)
                .innerJoin(BannedUsers, { Dailies.receivedById }, { BannedUsers.userId })
                .selectAll()
                .where {
                    BrowserFingerprints.clientId inList clientIds and (BannedUsers.userId inSubQuery validBannedUsersList(
                        now.toEpochMilli()
                    ))
                }
                .toList()
        }

        for (user in dailiesRecentlyRetrievedHours) {
            if (user[Dailies.receivedById] in alreadyChecked)
                continue

            val bannedUsersAssociatedWithThisUser = clientIdsThatAreBanned.filter { it[BrowserFingerprints.clientId] == user[BrowserFingerprints.clientId] }

            if (bannedUsersAssociatedWithThisUser.isNotEmpty()) {
                println("User to be banned ID: ${user[Dailies.receivedById]}")
                println(bannedUsersAssociatedWithThisUser)
                alreadyChecked.add(user[Dailies.receivedById])
            }
        }

        println("Epoch millis: ${now.toEpochMilli()}")
        println("Finished! $alreadyChecked")
    }
}