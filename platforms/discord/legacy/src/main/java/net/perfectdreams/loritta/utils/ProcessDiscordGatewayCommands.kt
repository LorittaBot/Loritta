package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import org.postgresql.jdbc.PgConnection
import kotlin.time.Duration.Companion.seconds

class ProcessDiscordGatewayCommands(private val loritta: Loritta, private val dataSource: HikariDataSource) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        while (true) {
            try {
                dataSource.connection
                    .use {
                        // Unwrap must be within the ".use" block to avoid connection leaks when PostgreSQL goes down!
                        val connection = it.unwrap(PgConnection::class.java)

                        for (shardId in loritta.lorittaCluster.minShard..loritta.lorittaCluster.maxShard) {
                            val stmt = connection.createStatement()
                            stmt.execute("LISTEN gateway_commands_shard_$shardId;")
                            stmt.close()
                        }
                        connection.commit()

                        while (connection.isValid(3.seconds.inWholeSeconds.toInt())) { // Validate if the connection is still alive to avoid network issues - https://github.com/pgjdbc/pgjdbc/issues/1144#issuecomment-1219818764
                            // We don't want to block forever, so we can validate if the connection is still valid or not
                            val notifications = connection.getNotifications(60_000)

                            for (notification in notifications) {
                                val shardId = notification.name.substringAfterLast("_").toInt()

                                val payload = notification.parameter
                                val jdaShard = lorittaShards.shardManager.getShardById(shardId) as JDAImpl?

                                if (jdaShard != null) {
                                    logger.info { "Sending gateway command $payload to $shardId" }
                                    jdaShard.client.send(DataObject.fromJson(notification.parameter))
                                } else {
                                    logger.warn { "Received a gateway event notification for a shard that we don't handle (shard ID: $shardId)! This should never happen!" }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }

            logger.warn { "Left the notification connection block, this may mean that the connection is dead! Trying to reconnect..." }
        }
    }
}