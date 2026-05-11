package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.CollectedReactionEventPoints
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventDrops
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventPlayers
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.alias
import java.io.File
import java.time.Duration

data class UserDropAnalysis(
    val userId: Long,
    val totalDrops: Int,
    val totalSwitches: Int,
    val lessThan1sSwitches: Int,
    val lessThan2sSwitches: Int,
    val lessThan3sSwitches: Int,
    val lessThan4sSwitches: Int,
    val lessThan5sSwitches: Int,
    val quickestSwitch: Duration?,
    val longestSwitch: Duration?,
    val averageSwitch: Duration?,
    val medianSwitch: Duration?,
    val averageSwitchLessThan1s: Duration?,
    val medianSwitchLessThan1s: Duration?
)

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

    val eventName = "Anniversary2026"
    val minimumDrops = 500

    println("Fetching all drop data for event $eventName...")

    // Single query: fetch all drop data for the event, ordered by user and collection time
    val allRows = pudding.transaction {
        ReactionEventDrops
            .innerJoin(CollectedReactionEventPoints)
            .innerJoin(ReactionEventPlayers)
            .select(
                ReactionEventPlayers.userId,
                ReactionEventDrops.channelId,
                ReactionEventDrops.createdAt,
                CollectedReactionEventPoints.collectedAt,
                (CollectedReactionEventPoints.collectedAt - ReactionEventDrops.createdAt).alias("timeDifference")
            )
            .where { ReactionEventDrops.event eq eventName }
            .orderBy(
                ReactionEventPlayers.userId to SortOrder.ASC,
                CollectedReactionEventPoints.collectedAt to SortOrder.ASC
            )
            .toList()
    }

    println("Fetched ${allRows.size} total rows, grouping by user...")

    // Group rows by userId
    val rowsByUser = allRows.groupBy { it[ReactionEventPlayers.userId] }

    // Filter to users with more than minimumDrops
    val eligibleUsers = rowsByUser.filter { it.value.size >= minimumDrops }

    println("Found ${eligibleUsers.size} players with more than $minimumDrops drops")
    println("=".repeat(60))
    println()

    val results = mutableListOf<UserDropAnalysis>()

    for ((index, entry) in eligibleUsers.entries.withIndex()) {
        val (userId, rows) = entry

        println("[${index + 1}/${eligibleUsers.size}] Analyzing user $userId (${rows.size} drops)...")

        var lastChannelData: ResultRow? = null
        val switches = mutableListOf<Duration>()
        for (row in rows) {
            if (lastChannelData != null && row[ReactionEventDrops.channelId] != lastChannelData[ReactionEventDrops.channelId]) {
                val diff = Duration.between(
                    lastChannelData[CollectedReactionEventPoints.collectedAt],
                    row[CollectedReactionEventPoints.collectedAt]
                )

                switches.add(diff)
            }
            lastChannelData = row
        }

        val lessThan1s = switches.filter { 1_000 > it.toMillis() }

        results.add(
            UserDropAnalysis(
                userId = userId,
                totalDrops = rows.size,
                totalSwitches = switches.size,
                lessThan1sSwitches = lessThan1s.size,
                lessThan2sSwitches = switches.filter { 2_000 > it.toMillis() }.size,
                lessThan3sSwitches = switches.filter { 3_000 > it.toMillis() }.size,
                lessThan4sSwitches = switches.filter { 4_000 > it.toMillis() }.size,
                lessThan5sSwitches = switches.filter { 5_000 > it.toMillis() }.size,
                quickestSwitch = switches.minOrNull(),
                longestSwitch = switches.maxOrNull(),
                averageSwitch = switches.average(),
                medianSwitch = switches.median(),
                averageSwitchLessThan1s = lessThan1s.average(),
                medianSwitchLessThan1s = lessThan1s.median()
            )
        )
    }

    // Sort by most suspicious (most sub-1s switches) to least suspicious
    val sorted = results.sortedByDescending { it.lessThan1sSwitches }

    println()
    println("=".repeat(60))
    println("Results (most suspicious -> least suspicious)")
    println("=".repeat(60))
    println()

    for (result in sorted) {
        println("User ${result.userId} (Total: ${result.totalDrops}):")
        println("  Total Switches: ${result.totalSwitches}")
        println("  Less than 1s switches: ${result.lessThan1sSwitches}")
        println("  Less than 2s switches: ${result.lessThan2sSwitches}")
        println("  Less than 3s switches: ${result.lessThan3sSwitches}")
        println("  Less than 4s switches: ${result.lessThan4sSwitches}")
        println("  Less than 5s switches: ${result.lessThan5sSwitches}")
        println("  Quickest Switch: ${result.quickestSwitch}")
        println("  Longest Switch: ${result.longestSwitch}")
        println("  Average Switch: ${result.averageSwitch}")
        println("  Median Switch: ${result.medianSwitch}")
        println("  Average Switch (Less than 1s): ${result.averageSwitchLessThan1s}")
        println("  Median Switch (Less than 1s): ${result.medianSwitchLessThan1s}")
        println()
    }
}
