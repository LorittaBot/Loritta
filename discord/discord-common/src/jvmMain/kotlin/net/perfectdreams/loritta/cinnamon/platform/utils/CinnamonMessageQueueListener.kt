package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.Snowflake
import dev.kord.rest.json.request.DMCreateRequest
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.pudding.data.DirectMessageUserDailyTaxTaxedMessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.DirectMessageUserDailyTaxWarnMessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.MessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownMessageQueuePayload

fun CinnamonMessageQueueListener(m: LorittaCinnamon, logger: KLogger = KotlinLogging.logger {}): (MessageQueuePayload) -> (Boolean) = {
    // TODO: Do everything blocking? This way if a error is thrown, we can just return false and then the job won't be removed from the db
    // 17:18:41.465 [DefaultDispatcher-worker-1] DEBUG [R]:[KTOR]:[ExclusionRequestRateLimiter] - [RESPONSE]:403:POST:https://discord.com/api/v9/channels/944325028885971016/messages body:{"message": "Cannot send messages to this user", "code": 50007}
    // It is blocking because we need to return if the job was completed or not
    when (it) {
        is DirectMessageUserDailyTaxTaxedMessageQueuePayload -> runBlocking {
            // 17:18:41.471 [DefaultDispatcher-worker-1] WARN net.perfectdreams.loritta.cinnamon.platform.utils.CinnamonMessageQueueListener - Something went wrong while trying to send a message to UserId(value=828958852590075934)! Invalidating cached private channel if present...
            //dev.kord.rest.request.KtorRequestException: REST request returned an error: 403 Forbidden  Cannot send messages to this user null
            val userId = it.userId
            val cachedChannelId = m.services.users.getCachedDiscordDirectMessageChannel(userId)
            val channelId = cachedChannelId ?: run {
                val id = m.rest.user.createDM(DMCreateRequest(Snowflake(userId.value.toLong()))).id.value.toLong()
                m.services.users.insertOrUpdateCachedDiscordDirectMessageChannel(userId, id)
                id
            }

            try {
                m.rest.channel.createMessage(
                    Snowflake(channelId)
                ) {
                    content = "kk olha o imposto que você levou, você tinha ${it.currentSonhos} sonhos e perdeu ${it.howMuchWasRemoved}, kk imposto é roubo mas o governo precisa de imposto para sustentar a Lorittalnad sorry"
                }
            } catch (e: KtorRequestException) {
                logger.warn(e) { "Something went wrong while trying to send a message to $userId! Invalidating cached private channel if present..." }
                m.services.users.deleteCachedDiscordDirectMessageChannel(userId)
            }
            true
        }
        is DirectMessageUserDailyTaxWarnMessageQueuePayload -> runBlocking {
            val userId = it.userId
            m.sendMessageToUserViaDirectMessage(
                userId
            ) {
                content = "Olá! Se você não pegar daily até as <t:${it.time.epochSeconds}:f>, você perderá ${it.howMuchWillBeRemoved} sonhos!"
            }

            true
        }
        UnknownMessageQueuePayload -> false
        else -> false
    }
}