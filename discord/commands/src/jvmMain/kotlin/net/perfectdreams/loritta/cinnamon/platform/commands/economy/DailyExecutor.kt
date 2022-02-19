package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinLocalDateTime
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.cinnamon.common.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.DailyCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.SonhosUtils.userHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.platform.utils.getUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.data.Daily
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DailyExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(DailyExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        // TODO: Do not hardcode the timezone
        val dailyResetZoneId = ZoneId.of("America/Sao_Paulo")
        val dailyTaxZoneOffset = ZoneOffset.UTC
        val todayAtMidnight = ZonedDateTime.now(dailyResetZoneId)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val userId = UserId(context.user.id.value)
        val todayDailyReward = context.loritta.services.sonhos.getUserLastDailyRewardReceived(
            userId,
            todayAtMidnight.toLocalDateTime().toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
        )

        val tomorrowAtMidnight = ZonedDateTime.now(dailyResetZoneId)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1L)

        if (todayDailyReward != null) {
            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(DailyCommand.I18N_PREFIX.PleaseWait("<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:R>")),
                    Emotes.Error
                )

                userHaventGotDailyTodayOrUpsellSonhosBundles(
                    context.loritta,
                    context.i18nContext,
                    UserId(context.user.id.value),
                    "daily",
                    "please-wait-daily-reset"
                )
            }
            return
        }

        val profile = context.loritta.services.users.getUserProfile(context.user)
        var currentUserThreshold: DailyTaxThresholds.DailyTaxThreshold? = null
        var userLastDailyReward: Daily? = null
        if (profile != null) {
            currentUserThreshold = DailyTaxThresholds.THRESHOLDS.firstOrNull { profile.money >= it.minimumSonhosForTrigger }
            if (currentUserThreshold != null) {
                userLastDailyReward = context.loritta.services.sonhos.getUserLastDailyRewardReceived(
                    userId,
                    Instant.DISTANT_PAST
                )
            }
        }

        val url = if (context is GuildApplicationCommandContext)
            "${context.loritta.config.website}daily?guild=${context.guildId.value}"
        else // Used for daily multiplier priority
            "${context.loritta.config.website}daily"

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(DailyCommand.I18N_PREFIX.DailyLink(url, "<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:t>")),
                Emotes.LoriRich
            )

            val todayDailyTaxTimeEpoch = OffsetDateTime.now(dailyTaxZoneOffset)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusDays(1)
                .toEpochSecond()

            // Check if the user is in a daily tax bracket and, if yes, tell to the user about it
            if (currentUserThreshold != null) {
                if (userLastDailyReward != null) {
                    // User is in a daily tax bracket and has received daily before
                    val whenYouAreGoingToStartToLoseSonhos = userLastDailyReward.receivedAt.toJavaInstant()
                        .atZone(dailyTaxZoneOffset)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(currentUserThreshold.maxDayThreshold)

                    if (OffsetDateTime.now(dailyTaxZoneOffset) > whenYouAreGoingToStartToLoseSonhos.toOffsetDateTime()) {
                        // User is already losing sonhos
                        styled(
                            context.i18nContext.get(
                                DailyCommand.I18N_PREFIX.DailyTaxBracketInfo.UserIsAlreadyLosingSonhosDueToDailyTax(
                                    currentUserThreshold.minimumSonhosForTrigger,
                                    currentUserThreshold.maxDayThreshold,
                                    currentUserThreshold.tax,
                                    "<t:$todayDailyTaxTimeEpoch:R>",
                                    "<t:$todayDailyTaxTimeEpoch:f>"
                                )
                            ),
                            Emotes.LoriCoffee
                        )
                    } else {
                        // User will lose sonhos in the future
                        val whenYouAreGoingToStartToLoseSonhosEpoch = whenYouAreGoingToStartToLoseSonhos.toEpochSecond()
                        styled(
                            context.i18nContext.get(
                                DailyCommand.I18N_PREFIX.DailyTaxBracketInfo.UserWillLoseSonhosInTheFuture(
                                    currentUserThreshold.minimumSonhosForTrigger,
                                    currentUserThreshold.maxDayThreshold,
                                    currentUserThreshold.tax,
                                    "<t:$whenYouAreGoingToStartToLoseSonhosEpoch:R>",
                                    "<t:$whenYouAreGoingToStartToLoseSonhosEpoch:f>"
                                )
                            ),
                            Emotes.LoriCoffee
                        )
                    }
                } else {
                    // User is in a daily tax bracket and has not received daily before
                    styled(
                        context.i18nContext.get(
                            DailyCommand.I18N_PREFIX.DailyTaxBracketInfo.UserIsAlreadyLosingSonhosDueToDailyTaxAndNeverGotDailyBefore(
                                currentUserThreshold.minimumSonhosForTrigger,
                                currentUserThreshold.maxDayThreshold,
                                currentUserThreshold.tax,
                                "<t:$todayDailyTaxTimeEpoch:R>",
                                "<t:$todayDailyTaxTimeEpoch:f>",
                            )
                        ),
                        Emotes.LoriCoffee
                    )
                }
            }

            styled(
                context.i18nContext.get(DailyCommand.I18N_PREFIX.DailyWarning("${context.loritta.config.website}guidelines")),
                Emotes.LoriBanHammer
            )

            styled(
                context.i18nContext.get(
                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                        context.loritta.config.website,
                        "daily",
                        "daily-reward"
                    )
                ),
                Emotes.CreditCard
            )
        }
    }
}