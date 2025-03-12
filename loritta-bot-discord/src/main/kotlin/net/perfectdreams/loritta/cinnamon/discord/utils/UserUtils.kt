package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.toJavaInstant
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.serializable.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.serializable.UserId

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
        loritta: LorittaBot,
        pudding: Pudding,
        userId: UserId,
        messageBuilder: InlineMessage<*>.() -> (Unit)
    ) = sendMessageToUserViaDirectMessage(
        loritta,
        pudding,
        userId,
        dev.minn.jda.ktx.messages.MessageCreate { apply(messageBuilder) }
    )

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     *
     * @return if the message was successfully sent, a message may fail to be sent if the channel does not exist or if the user disabled their DMs
     */
    suspend fun sendMessageToUserViaDirectMessage(
        loritta: LorittaBot,
        pudding: Pudding,
        userId: UserId,
        request: MessageCreateData
    ): Boolean {
        return try {
            val privateChannel = loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(userId.value.toLong())

            // Unknown user
            if (privateChannel == null)
                return false

            privateChannel.sendMessage(request).await()
            true
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to send a message to $userId!" }
            false
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        lorittaWebsiteUrl: String,
        userId: UserId,
        data: DailyTaxWarnUserNotification
    ): InlineMessage<*>.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.TitleWarning)

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

            color = LorittaColors.LorittaRed.rgb

            timestamp = data.timestamp.toJavaInstant()

            image = "https://stuff.loritta.website/loritta-sonhos-drool-cooki.png"
        }
    }

    /**
     * Builds a daily tax message for the [data]
     */
    fun buildDailyTaxMessage(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        lorittaWebsiteUrl: String,
        userId: UserId,
        data: DailyTaxTaxedUserNotification
    ): InlineMessage<*>.() -> Unit = {
        embed {
            title = i18nContext.get(I18nKeysData.InactiveDailyTax.TitleTaxed)

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

            color = LorittaColors.LorittaRed.rgb

            timestamp = data.timestamp.toJavaInstant()

            image = "https://stuff.loritta.website/loritta-sonhos-running-cooki.png"
        }
    }
}