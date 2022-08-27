package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import java.time.LocalDateTime
import java.time.ZoneId

object SonhosUtils {
    fun insufficientSonhos(profile: PuddingUserProfile?, howMuch: Long) = insufficientSonhos(profile?.money ?: 0L, howMuch)
    fun insufficientSonhos(sonhos: Long, howMuch: Long) = I18nKeysData.Commands.InsufficientFunds(howMuch, howMuch - sonhos)

    suspend fun MessageBuilder.userHaventGotDailyTodayOrUpsellSonhosBundles(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        userId: UserId,
        upsellMedium: String,
        upsellCampaignContent: String
    ) {
        // TODO: Do not hardcode the timezone
        val now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayDailyReward = loritta.services.sonhos.getUserLastDailyRewardReceived(
            userId,
            now.toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
        )

        if (todayDailyReward != null) {
             // Already got their daily reward today, show our sonhos bundles!
            styled(
                i18nContext.get(
                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                        loritta.config.loritta.website,
                        upsellMedium,
                        upsellCampaignContent
                    )
                ),
                Emotes.CreditCard
            )
        } else {
            // Recommend the user to get their daily reward
            styled(
                i18nContext.get(I18nKeysData.Commands.WantingMoreSonhosDaily(loritta.commandMentions.daily)),
                Emotes.Gift
            )
        }
    }
}