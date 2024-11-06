package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.CollectedReactionEventPoints
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventDrops
import net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents.ReactionEventPlayers
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.alias
import java.io.File
import java.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

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

    val ids = listOf(
        Pair("Paum", 197501878399926272L),
        Pair("Sté", 400683515873591296L),
        Pair("Breno", 196798877725229057L),
        Pair("Power", 123170274651668480L),
        Pair("Suspeito", 1122698122662006834L),
        Pair("Suspeito2", 759208002300936212L),
        Pair("Suspeito3", 252157358749253632L),
        Pair("Suspeito4", 798549768137474058L),
        Pair("Suspeito5", 879724516065046598L),
        Pair("Suspeito6", 345001852716318721L)
    )

    for (id in ids) {
        val stuff = pudding.transaction {
            ReactionEventDrops
                .innerJoin(CollectedReactionEventPoints)
                .innerJoin(ReactionEventPlayers)
                .select(
                    ReactionEventDrops.channelId,
                    ReactionEventDrops.createdAt,
                    CollectedReactionEventPoints.collectedAt,
                    (CollectedReactionEventPoints.collectedAt - ReactionEventDrops.createdAt).alias("timeDifference")
                )
                .where {
                    ReactionEventPlayers.userId eq id.second
                }
                .orderBy(CollectedReactionEventPoints.collectedAt, SortOrder.ASC)
                .toList()
        }

        var lastChannelData: ResultRow? = null
        val switches = mutableListOf<Duration>()
        for (row in stuff) {
            if (lastChannelData != null && row[ReactionEventDrops.channelId] != lastChannelData[ReactionEventDrops.channelId]) {
                val diff = Duration.between(
                    lastChannelData[CollectedReactionEventPoints.collectedAt],
                    row[CollectedReactionEventPoints.collectedAt]
                )

                // println("Channel Switch ${lastChannelData[ReactionEventDrops.channelId]} -> ${row[ReactionEventDrops.channelId]}: $diff")
                switches.add(diff)
            }
            lastChannelData = row
        }

        println(id.first + " (${id.second}):")
        println("Total Switches: ${switches.size}")
        println("Less than 1s switches: ${switches.filter { 1_000 > it.toMillis() }.size}")
        println("Less than 2s switches: ${switches.filter { 2_000 > it.toMillis() }.size}")
        println("Less than 3s switches: ${switches.filter { 3_000 > it.toMillis() }.size}")
        println("Less than 4s switches: ${switches.filter { 4_000 > it.toMillis() }.size}")
        println("Less than 5s switches: ${switches.filter { 5_000 > it.toMillis() }.size}")
        println("Quickest Switch: ${switches.min()}")
        println("Longest Switch: ${switches.max()}")
        println("Average Switch: ${switches.average()}")
        println("Median Switch: ${switches.median()}")
        println("Average Switch (Less than 1s): ${switches.filter { 1_000 > it.toMillis() }.average()}")
        println("Median Switch (Less than 1s): ${switches.filter { 1_000 > it.toMillis() }.median()}")
        println()
    }
}

fun List<Duration>.average(): Duration? {
    if (isEmpty()) return null  // Handle empty list case
    return (this.map { it.toKotlinDuration() }.reduce { acc, duration -> acc + duration } / this.size).toJavaDuration()
}

fun List<Duration>.median(): Duration? {
    if (isEmpty()) return null // Handle empty list case

    val sortedDurations = this.sorted()
    val middleIndex = sortedDurations.size / 2

    return if (sortedDurations.size % 2 == 1) {
        // Odd number of elements: return the middle element
        sortedDurations[middleIndex]
    } else {
        // Even number of elements: average the two middle elements
        Duration.ofMillis(sortedDurations[middleIndex - 1].toMillis() + sortedDurations[middleIndex].toMillis() / 2)
    }
}