package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils

import dev.kord.rest.service.RestClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.PendingImportantNotificationState
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.ImportantNotificationDatabaseMessage
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications

/**
 * Processes correios pending messages from our message queue.
 */
class PendingImportantNotificationsProcessor(
    private val lorittaConfig: LorittaConfig,
    private val pudding: Pudding,
    private val rest: RestClient
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        try {
            logger.info { "Processing pending important notifications in the queue..." }

            val connection = pudding.hikariDataSource.connection
            connection.use {
                val selectStatement = it.prepareStatement("""SELECT id, "user", state, message FROM ${PendingImportantNotifications.tableName} WHERE state = '${PendingImportantNotificationState.PENDING.name}' ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;""")
                val rs = selectStatement.executeQuery()

                var count = 0
                while (rs.next()) {
                    val id = rs.getLong("id")
                    val userId = rs.getLong("user")
                    val state = rs.getString("state")
                    val messageAsString = rs.getString("message")

                    val message = Json.decodeFromString<ImportantNotificationDatabaseMessage>(messageAsString)

                    count++

                    val messageWasSuccessfullySent = runBlocking {
                        UserUtils.sendMessageToUserViaDirectMessage(
                            pudding,
                            rest,
                            UserId(userId),
                            message.toMultipartMessageCreateRequest()
                        )
                    }

                    logger.info { "Sent direct message to $userId! Success? $messageWasSuccessfullySent" }

                    // https://www.gotoquiz.com/web-coding/programming/java-programming/convert-between-java-enums-and-postgresql-enums/
                    val updateStatement = it.prepareStatement("UPDATE ${PendingImportantNotifications.tableName} SET state = CAST(? AS ${PendingImportantNotificationState::class.simpleName!!.lowercase()}) WHERE id = ?;")
                    updateStatement.setObject(
                        1,
                        (if (messageWasSuccessfullySent) PendingImportantNotificationState.SUCCESSFULLY_SENT_VIA_DIRECT_MESSAGE else PendingImportantNotificationState.FAILED_TO_SEND_VIA_DIRECT_MESSAGE).name
                    )

                    updateStatement.setLong(2, id)
                    updateStatement.execute()
                }

                it.commit()

                logger.info { "Successfully processed $count pending important notifications in the queue!" }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while polling pending important notifications in the queue!" }
        }
    }
}