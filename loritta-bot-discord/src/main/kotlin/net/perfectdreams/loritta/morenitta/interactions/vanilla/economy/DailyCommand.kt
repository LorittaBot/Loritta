package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import kotlinx.datetime.*
import kotlinx.datetime.Instant
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.serializable.Daily
import net.perfectdreams.loritta.serializable.UserId
import java.time.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Daily
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        enableLegacyMessageSupport = true
        alternativeLegacyLabels.apply {
            add("diário")
            add("bolsafamília")
        }

        executor = DailyExecutor()
    }

    inner class DailyExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guildOrNull

            context.deferChannelMessage(true)

            // TODO: Do not hardcode the timezone
            val dailyResetZoneId = ZoneId.of("America/Sao_Paulo")
            val dailyTaxZoneOffset = ZoneOffset.UTC
            val todayAtMidnight = ZonedDateTime.now(dailyResetZoneId)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            val userId = UserId(context.user.idLong)
            val todayDailyReward = context.loritta.pudding.sonhos.getUserLastDailyRewardReceived(
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
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.PleaseWait("<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:R>")),
                        Emotes.Error
                    )

                    appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                        context.loritta,
                        context.i18nContext,
                        UserId(context.user.idLong),
                        "daily",
                        "please-wait-daily-reset"
                    )
                }
                return
            }

            val profile = context.loritta.pudding.users.getUserProfile(UserId(context.user.idLong))
            var currentUserThreshold: DailyTaxThresholds.DailyTaxThreshold? = null
            var userLastDailyReward: Daily? = null
            if (profile != null) {
                currentUserThreshold = DailyTaxThresholds.THRESHOLDS.firstOrNull { profile.money >= it.minimumSonhosForTrigger }
                if (currentUserThreshold != null) {
                    userLastDailyReward = context.loritta.pudding.sonhos.getUserLastDailyRewardReceived(
                        userId,
                        Instant.DISTANT_PAST
                    )
                }
            }

            val url = if (guild != null)
                GACampaigns.dailyWebRewardDiscordCampaignUrl(
                    context.loritta.config.loritta.website.url,
                    "daily",
                    "cmd-with-multiplier"
                ) + "&guild=${guild.idLong}"
            else // Used for daily multiplier priority
                GACampaigns.dailyWebRewardDiscordCampaignUrl(
                    context.loritta.config.loritta.website.url,
                    "daily",
                    "cmd-without-multiplier"
                )

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.DailyLink(url, "<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:t>")),
                    Emotes.LoriRich
                )

                // Upsell stuff - Remove later!
                val now = java.time.Instant.now()
                val upsellStartsAt = LocalDateTime.of(2023, 12, 1, 13, 0)
                    .atZone(dailyResetZoneId)
                    .toInstant()
                val upsellEndsAt = LocalDateTime.of(2023, 12, 2, 13, 0)
                    .atZone(dailyResetZoneId)
                    .toInstant()

                if (now in upsellStartsAt..upsellEndsAt) {
                    styled(
                        "Ei! Já pensou em criar um jogo mas nunca soube codar, e poder fazer tudo isso no seu celular?! Você encontrou o lugar certo! A Loritta recebeu 100 acessos exclusivos pro app da Soba. Basta usar o codigo \"LORITTA\" ao se registrar no site deles! https://soba.xyz. Não perca!",
                        Emotes.LoriWow
                    )
                } else {
                    val todayDailyTaxTimeEpoch = OffsetDateTime.now(dailyTaxZoneOffset)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(1)
                        .toEpochSecond()

                    // Check if the user is in a daily tax bracket and, if yes, tell to the user about it
                    if (currentUserThreshold != null) {
                        val activeUserPayments =
                            context.loritta.pudding.payments.getActiveMoneyFromDonations(UserId(context.user.idLong))
                        val activeUserPremiumPlan = UserPremiumPlans.getPlanFromValue(activeUserPayments)

                        if (activeUserPremiumPlan.hasDailyInactivityTax) {
                            if (userLastDailyReward != null) {
                                // User is in a daily tax bracket and has received daily before
                                val whenYouAreGoingToStartToLoseSonhos = userLastDailyReward.receivedAt.toJavaInstant()
                                    .atZone(dailyTaxZoneOffset)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0)
                                    .plusDays(currentUserThreshold.maxDayThreshold.toLong())

                                if (OffsetDateTime.now(dailyTaxZoneOffset) > whenYouAreGoingToStartToLoseSonhos.toOffsetDateTime()) {
                                    // User is already losing sonhos
                                    styled(
                                        context.i18nContext.get(
                                            I18N_PREFIX.DailyTaxBracketInfo.UserIsAlreadyLosingSonhosDueToDailyTax(
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
                                    val whenYouAreGoingToStartToLoseSonhosEpoch =
                                        whenYouAreGoingToStartToLoseSonhos.toEpochSecond()
                                    styled(
                                        context.i18nContext.get(
                                            I18N_PREFIX.DailyTaxBracketInfo.UserWillLoseSonhosInTheFuture(
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
                                        I18N_PREFIX.DailyTaxBracketInfo.UserIsAlreadyLosingSonhosDueToDailyTaxAndNeverGotDailyBefore(
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
                        } else {
                            // User is in a daily tax bracket, but they don't have the daily inactivity tax
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.DailyTaxBracketInfo.UserDoesntHaveDailyTaxBecauseTheyArePremium(
                                        currentUserThreshold.minimumSonhosForTrigger,
                                        currentUserThreshold.maxDayThreshold,
                                        currentUserThreshold.tax,
                                        Emotes.LoriKiss
                                    )
                                ),
                                Emotes.LoriCoffee
                            )
                        }
                    }

                    styled(
                        context.i18nContext.get(I18N_PREFIX.DailyWarning("${context.loritta.config.loritta.website.url}guidelines")),
                        Emotes.LoriBanHammer
                    )

                    styled(
                        context.i18nContext.get(
                            GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                context.loritta.config.loritta.website.url,
                                "daily",
                                "daily-reward"
                            )
                        ),
                        Emotes.CreditCard
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}