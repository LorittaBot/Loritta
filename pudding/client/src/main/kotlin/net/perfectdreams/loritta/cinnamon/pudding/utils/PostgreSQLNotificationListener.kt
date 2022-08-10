package net.perfectdreams.loritta.cinnamon.pudding.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import org.postgresql.jdbc.PgConnection

class PostgreSQLNotificationListener(
    private val pudding: Pudding,
    private val callbacks: Map<String, (String) -> (Unit)>
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        while (true) {
            try {
                pudding.hikariDataSource.connection
                    .unwrap(PgConnection::class.java)
                    .use { pgConnection ->
                        val stmt = pgConnection.createStatement()
                        for (channel in callbacks.keys) {
                            stmt.execute("LISTEN $channel;")
                        }
                        stmt.close()
                        pgConnection.commit()

                        while (true) {
                            val notifications = pgConnection.getNotifications(Int.MAX_VALUE)

                            for (notification in notifications) {
                                try {
                                    logger.info { "Received notification ${notification.name}: ${notification.parameter}" }

                                    val callback = callbacks[notification.name]
                                    callback?.invoke(notification.parameter)
                                } catch (e: Exception) {
                                    logger.warn(e) { "Something went wrong while processing PostgreSQL notification ${notification.name}! We will ignore it..." }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while listening for PostgreSQL notifications! We will restart the connection..." }
            }
        }
    }
}