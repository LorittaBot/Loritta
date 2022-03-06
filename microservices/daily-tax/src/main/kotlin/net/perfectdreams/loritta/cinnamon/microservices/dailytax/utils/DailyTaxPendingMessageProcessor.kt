package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import dev.kord.rest.service.RestClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.DailyTaxPendingDirectMessageState
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordUserMessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxPendingDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxTaxedDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxWarnDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxPendingDirectMessages

/**
 * Processes daily tax pending messages from our message queue.
 */
class DailyTaxPendingMessageProcessor(
    private val lorittaConfig: LorittaConfig,
    private val pudding: Pudding,
    private val rest: RestClient,
    private val languageManager: LanguageManager
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        // TODO: proper i18n
        val i18nContext = languageManager.getI18nContextById("pt")

        try {
            logger.info { "Processing pending daily tax messages in the queue..." }

            val connection = pudding.hikariDataSource.connection
            connection.use {
                val selectStatement = it.prepareStatement("""SELECT id, "user", state, data FROM ${DailyTaxPendingDirectMessages.tableName} WHERE state = '${DailyTaxPendingDirectMessageState.PENDING.name}' ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;""")
                val rs = selectStatement.executeQuery()

                var count = 0
                while (rs.next()) {
                    val id = rs.getLong("id")
                    val userId = rs.getLong("user")
                    val state = rs.getString("state")
                    val dataAsString = rs.getString("data")

                    val pendingDailyTaxDirectMessage = Json.decodeFromString<DailyTaxPendingDirectMessage>(dataAsString)

                    count++

                    // TODO: Add builder conversion instead of having multiple methods for each builder
                    val realUserId = UserId(userId)
                    val builder = when (pendingDailyTaxDirectMessage) {
                        is UserDailyTaxTaxedDirectMessage -> UserUtils.buildDailyTaxMessage(i18nContext, lorittaConfig.website, realUserId, pendingDailyTaxDirectMessage)
                        is UserDailyTaxWarnDirectMessage -> UserUtils.buildDailyTaxMessage(i18nContext, lorittaConfig.website, realUserId, pendingDailyTaxDirectMessage)
                    }.toKordUserMessageCreateBuilder()

                    val messageWasSuccessfullySent = runBlocking {
                        UserUtils.sendMessageToUserViaDirectMessage(
                            pudding,
                            rest,
                            UserId(userId),
                            builder
                        )
                    }

                    // https://www.gotoquiz.com/web-coding/programming/java-programming/convert-between-java-enums-and-postgresql-enums/
                    val updateStatement = it.prepareStatement("UPDATE ${DailyTaxPendingDirectMessages.tableName} SET state = CAST(? AS ${DailyTaxPendingDirectMessageState::class.simpleName!!.lowercase()}) WHERE id = ?;")
                    updateStatement.setObject(1, (if (messageWasSuccessfullySent) DailyTaxPendingDirectMessageState.SUCCESSFULLY_SENT_VIA_DIRECT_MESSAGE else DailyTaxPendingDirectMessageState.FAILED_TO_SEND_VIA_DIRECT_MESSAGE).name)
                    updateStatement.setLong(2, id)
                    updateStatement.execute()
                }

                it.commit()

                logger.info { "Successfully processed $count pending daily tax tasks in the queue!" }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while polling pending daily tax tasks in the queue!" }
        }
    }
}