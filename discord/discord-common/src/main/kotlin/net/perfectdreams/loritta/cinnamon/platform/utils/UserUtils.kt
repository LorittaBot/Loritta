package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.json.request.DMCreateRequest
import dev.kord.rest.json.request.MultipartMessageCreateRequest
import dev.kord.rest.request.KtorRequestException
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxTaxedDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxWarnDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object UserUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     *
     * @return if the message was successfully sent, a message may fail to be sent if the channel does not exist or if the user disabled their DMs
     */
    suspend fun sendMessageToUserViaDirectMessage(
        pudding: Pudding,
        rest: RestClient,
        userId: UserId,
        builder: UserMessageCreateBuilder.() -> (Unit)
    ) = sendMessageToUserViaDirectMessage(
        pudding,
        rest,
        userId,
        UserMessageCreateBuilder().apply(builder).toRequest()
    )

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     *
     * @return if the message was successfully sent, a message may fail to be sent if the channel does not exist or if the user disabled their DMs
     */
    suspend fun sendMessageToUserViaDirectMessage(
        pudding: Pudding,
        rest: RestClient,
        userId: UserId,
        request: MultipartMessageCreateRequest
    ): Boolean {
        val cachedChannelId = pudding.users.getCachedDiscordDirectMessageChannel(userId)

        return try {
            val channelId = cachedChannelId ?: run {
                val id = rest.user.createDM(DMCreateRequest(Snowflake(userId.value.toLong()))).id.value.toLong()
                pudding.users.insertOrUpdateCachedDiscordDirectMessageChannel(userId, id)
                id
            }

            rest.channel.createMessage(
                Snowflake(channelId),
                request
            )
            true
        } catch (e: KtorRequestException) {
            // 17:18:41.465 [DefaultDispatcher-worker-1] DEBUG [R]:[KTOR]:[ExclusionRequestRateLimiter] - [RESPONSE]:403:POST:https://discord.com/api/v9/channels/944325028885971016/messages body:{"message": "Cannot send messages to this user", "code": 50007}
            logger.warn(e) { "Something went wrong while trying to send a message to $userId! Invalidating cached direct message if present..." }
            pudding.users.deleteCachedDiscordDirectMessageChannel(userId)
            false
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(i18nContext: I18nContext, lorittaWebsiteUrl: String, userId: UserId, data: UserDailyTaxWarnDirectMessage): ImportantNotificationDatabaseMessageBuilder.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.Title)

            description = i18nContext.get(
                I18nKeysData.InactiveDailyTax.Warn(
                    user = "<@${userId.value}>",
                    sonhosTaxBracketThreshold = data.minimumSonhosForTrigger,
                    currentSonhos = data.currentSonhos,
                    daysWithoutGettingDaily = data.maxDayThreshold,
                    howMuchWillBeRemoved = data.howMuchWillBeRemoved,
                    taxPercentage = data.tax,
                    inactivityTaxTimeWillBeTriggeredAt = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:f>",
                    timeWhenDailyTaxIsTriggered = "<t:${data.inactivityTaxTimeWillBeTriggeredAt.epochSeconds}:t>",
                    dailyLink = GACampaigns.dailyWebRewardDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-warned-about-taxes"
                    ),
                    premiumLink = GACampaigns.premiumUpsellDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-warned-about-taxes"
                    )
                )
            ).joinToString("\n")

            color = LorittaColors.LorittaRed.toKordColor()

            timestamp = data.triggeredWarnAt

            image = lorittaWebsiteUrl + "v3/assets/img/sonhos/loritta_sonhos_drool.png"
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(i18nContext: I18nContext, lorittaWebsiteUrl: String, userId: UserId, data: UserDailyTaxTaxedDirectMessage): ImportantNotificationDatabaseMessageBuilder.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.Title)

            description = i18nContext.get(
                I18nKeysData.InactiveDailyTax.Taxed(
                    user = "<@${userId.value}>",
                    sonhosTaxBracketThreshold = data.minimumSonhosForTrigger,
                    howMuchWasRemoved = data.howMuchWasRemoved,
                    daysWithoutGettingDaily = data.maxDayThreshold,
                    taxPercentage = data.tax,
                    nextInactivityTaxTimeWillBeTriggeredAt = "<t:${data.nextInactivityTaxTimeWillBeTriggeredAt.epochSeconds}:f>",
                    timeWhenDailyTaxIsTriggered = "<t:${data.nextInactivityTaxTimeWillBeTriggeredAt.epochSeconds}:t>",
                    dailyLink = GACampaigns.dailyWebRewardDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-taxed"
                    ),
                    premiumLink = GACampaigns.premiumUpsellDiscordCampaignUrl(
                        lorittaWebsiteUrl,
                        "daily-tax-message",
                        "user-taxed"
                    )
                )
            ).joinToString("\n")

            color = LorittaColors.LorittaRed.toKordColor()

            timestamp = data.inactivityTaxTimeTriggeredAt

            image = lorittaWebsiteUrl + "v3/assets/img/sonhos/loritta_sonhos_running.png"
        }
    }
}