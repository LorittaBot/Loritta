package net.perfectdreams.loritta.cinnamon.discord.utils.directmessageprocessor

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.NotificationUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordUserMessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.common.utils.PendingImportantNotificationState
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.UserId

class PendingImportantNotificationsProcessor(val loritta: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        // TODO: proper i18nContext
        val i18nContext = loritta.languageManager.getI18nContextById("pt")

        try {
            logger.info { "Processing pending important notifications in the queue..." }

            val connection = loritta.pudding.hikariDataSource.connection
            connection.use {
                val selectStatement = it.prepareStatement("""SELECT id, "user", state, notification FROM ${PendingImportantNotifications.tableName} WHERE state = '${PendingImportantNotificationState.PENDING.name}' ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;""")
                val rs = selectStatement.executeQuery()

                var count = 0
                while (rs.next()) {
                    val id = rs.getLong("id")
                    val userId = rs.getLong("user")
                    val state = rs.getString("state")
                    val notificationId = rs.getLong("notification")

                    // TODO: This could be improved to reuse the same connection, however I don't know how this could be done without replacing everything with Exposed
                    val messageWasSuccessfullySent = runBlocking {
                        val notification = loritta.pudding.notifications.getUserNotification(
                            UserId(userId),
                            notificationId
                        )

                        if (notification == null) {
                            logger.warn { "Tried pulling information about notification with ID $notificationId to relay it, but it doesn't exist!" }

                            return@runBlocking false
                        } else {
                            val message = NotificationUtils.buildUserNotificationMessage(
                                loritta,
                                i18nContext,
                                notification,
                                loritta.config.loritta.website.url
                            )

                            return@runBlocking UserUtils.sendMessageToUserViaDirectMessage(
                                loritta.pudding,
                                loritta.rest,
                                UserId(userId),
                                message.toKordUserMessageCreateBuilder()
                            )
                        }
                    }

                    count++

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