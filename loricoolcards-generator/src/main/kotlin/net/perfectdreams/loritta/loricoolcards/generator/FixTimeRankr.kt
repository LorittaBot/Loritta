package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.CraftedReactionEventItems
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventFinishedEventUsers
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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

    val stuff = pudding.transaction {
        val cField = CraftedReactionEventItems.user.count()

        val rows = CraftedReactionEventItems.select(CraftedReactionEventItems.user, cField)
            .having { cField greaterEq 1_000 }
            .groupBy(CraftedReactionEventItems.user)
            .toList()

        for (row in rows) {
            val whenWasIt = CraftedReactionEventItems
                .selectAll()
                .where {
                    CraftedReactionEventItems.user eq row[CraftedReactionEventItems.user]
                }
                .orderBy(CraftedReactionEventItems.createdAt to SortOrder.ASC)
                .limit(1)
                .offset(999)
                .first()

            ReactionEventFinishedEventUsers.insert {
                it[ReactionEventFinishedEventUsers.user] = whenWasIt[CraftedReactionEventItems.user]
                it[ReactionEventFinishedEventUsers.event] = whenWasIt[CraftedReactionEventItems.event]
                it[ReactionEventFinishedEventUsers.finishedAt] = whenWasIt[CraftedReactionEventItems.createdAt]
            }
        }
    }
}