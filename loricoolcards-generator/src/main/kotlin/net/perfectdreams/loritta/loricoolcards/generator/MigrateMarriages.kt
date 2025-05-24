package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriagesOld
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.sql.Connection
import java.time.Instant

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

    val (marriedProfiles, oldMarriagesAll) = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
        val marriedProfiles = Profiles.selectAll()
            .where {
                Profiles.marriage.isNotNull()
            }
            .toList()

        val marryIds = marriedProfiles.map {
            it[Profiles.marriage]!!
        }

        val oldMarriages = MarriagesOld.selectAll().where {
            MarriagesOld.id inList marryIds
        }.toList()

        Pair(
            marriedProfiles,
            oldMarriages
        )
    }

    // Validation code
    if (true) {
        val newParticipants = pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
            MarriageParticipants.selectAll().map { it[MarriageParticipants.user] }
        }

        for ((index, marriage) in oldMarriagesAll.withIndex()) {
            val participants = marriedProfiles.filter { it[Profiles.marriage] == marriage[MarriagesOld.id] }

            if (participants.size != 2) {
                println("Couldn't find two participants for marriage ${marriage[MarriagesOld.id]} (found ${participants.size}), skipping...")
                continue
            }

            val hasU1 = newParticipants.contains(participants[0][Profiles.id].value)
            val hasU2 = newParticipants.contains(participants[1][Profiles.id].value)

            if (!hasU1) {
                println("Couldn't find user ${participants[0][Profiles.id].value} in the new participants list!")
            }

            if (!hasU2) {
                println("Couldn't find user ${participants[1][Profiles.id].value} in the new participants list!")
            }
        }

        println("Done!")
        return
    }

    val jobs = mutableListOf<Job>()

    for ((gIndex, oldMarriages) in oldMarriagesAll.chunked(250).withIndex()) {
        jobs.add(
            GlobalScope.launch {
                pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED, repetitions = 99999) {
                    for ((index, marriage) in oldMarriages.withIndex()) {
                        val participants = marriedProfiles.filter { it[Profiles.marriage] == marriage[MarriagesOld.id] }

                        if (participants.size != 2) {
                            println("Couldn't find two participants for marriage ${marriage[MarriagesOld.id]} (found ${participants.size}), skipping...")
                            continue
                        }

                        val createdAt = Instant.ofEpochMilli(marriage[MarriagesOld.marriedSince])
                        val marriage = UserMarriages.insertAndGetId {
                            it[UserMarriages.createdAt] = createdAt
                            it[UserMarriages.active] = true
                            // it[UserMarriages.affinity] = DEFAULT_AFFINITY
                            it[UserMarriages.hugCount] = 0
                            it[UserMarriages.headPatCount] = 0
                            it[UserMarriages.highFiveCount] = 0
                            it[UserMarriages.slapCount] = 0
                            it[UserMarriages.attackCount] = 0
                            it[UserMarriages.danceCount] = 0
                            it[UserMarriages.kissCount] = 0
                            it[UserMarriages.coupleName] = null
                        }

                        MarriageParticipants.batchInsert(participants) {
                            this[MarriageParticipants.marriage] = marriage
                            this[MarriageParticipants.user] = it[Profiles.id].value
                            this[MarriageParticipants.joinedAt] = createdAt
                            this[MarriageParticipants.primaryMarriage] = true
                        }

                        println("[$gIndex] Progress: $index/${oldMarriages.size}")
                    }
                }
            }
        )
    }

    jobs.joinAll()

    println("Finished! :3")
}