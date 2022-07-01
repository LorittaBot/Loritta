package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.tables.DiscordGatewayEvents

class ProcessDiscordGatewayEvents(
    private val m: DiscordGatewayEventsProcessor,
    private val queueDatabaseDataSource: HikariDataSource
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var totalEventsProcessed = 0

    override fun run() {
        while (true) {
            try {
                val connection = queueDatabaseDataSource.connection
                connection.use {
                    val selectStatement = it.prepareStatement("""SELECT id, "type", payload FROM ${DiscordGatewayEvents.tableName} ORDER BY id FOR UPDATE SKIP LOCKED LIMIT ${m.config.eventsPerBatch};""")
                    val rs = selectStatement.executeQuery()

                    var count = 0
                    val processedRows = mutableListOf<Long>()
                    val currentActiveEvents = m.modules.sumOf { it.activeEvents.size }

                    if (currentActiveEvents >= m.config.eventsPerBatch) {
                        logger.warn { "Too many current active events! We are going to query again in 100ms... $currentActiveEvents" }
                        Thread.sleep(100)
                        return@use
                    }

                    while (rs.next()) {
                        val id = rs.getLong("id")
                        val type = rs.getString("type")
                        val gatewayPayload = rs.getString("payload")

                        val discordEvent = KordDiscordEventUtils.parseEventFromJsonString(gatewayPayload)

                        if (discordEvent != null) {
                            for (module in m.modules) {
                                module.launchEventProcessorJob(discordEvent)
                            }
                        } else {
                            logger.warn { "Unknown Discord event received ($type)! We are going to ignore the event... kthxbye!" }
                        }

                        count++
                        totalEventsProcessed++

                        processedRows.add(id)
                    }

                    val deleteStatement = it.prepareStatement("DELETE FROM ${DiscordGatewayEvents.tableName} WHERE id = ANY(?)")
                    val array = connection.createArrayOf("bigint", processedRows.toTypedArray())
                    deleteStatement.setArray(1, array)
                    deleteStatement.execute()

                    it.commit()
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}