package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.components.button.Button
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendActiveReactionEventUpsellInformationIfNotNull
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendCouponSonhosBundleUpsellInformationIfNotNull
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventsAttributes
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.serializable.Daily
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class DailyCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Daily
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("e9fc464e-2064-445b-a17a-dfe89a3dbc87")) {
        enableLegacyMessageSupport = true
        alternativeLegacyLabels.apply {
            add("diário")
            add("bolsafamília")
        }
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = DailyExecutor()
    }

    inner class DailyExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guildOrNull

            context.deferChannelMessage(true)

            // TODO: Do not hardcode the timezone
            val dailyResetZoneId = ZoneId.of("America/Sao_Paulo")
            val dailyTaxZoneOffset = ZoneOffset.UTC
            val nowZDT = ZonedDateTime.now(dailyResetZoneId)
            val nowInstant = nowZDT.toInstant()

            val todayAtMidnight = nowZDT
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            val userId = UserId(context.user.idLong)
            val todayDailyReward = context.loritta.pudding.sonhos.getUserLastDailyRewardReceived(
                userId,
                todayAtMidnight.toLocalDateTime().toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
            )

            val tomorrowAtMidnight = nowZDT
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

            val claimedWebsiteCoupon = loritta.transaction {
                val couponData = WebsiteDiscountCoupons.selectAll()
                    .where {
                        WebsiteDiscountCoupons.public and (WebsiteDiscountCoupons.startsAt lessEq nowInstant and (WebsiteDiscountCoupons.endsAt greaterEq nowInstant))
                    }
                    .orderBy(WebsiteDiscountCoupons.total, SortOrder.ASC)
                    .firstOrNull()

                if (couponData != null) {
                    val paymentsThatUsedTheCouponCount = Payments.selectAll()
                        .where {
                            Payments.coupon eq couponData[WebsiteDiscountCoupons.id]
                        }
                        .count()

                    ClaimedWebsiteCoupon(
                        couponData[WebsiteDiscountCoupons.id].value,
                        couponData[WebsiteDiscountCoupons.code],
                        couponData[WebsiteDiscountCoupons.endsAt],
                        couponData[WebsiteDiscountCoupons.total],
                        couponData[WebsiteDiscountCoupons.maxUses],
                        paymentsThatUsedTheCouponCount,
                    )
                } else null
            }

            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.DailyLink(url, "<t:${tomorrowAtMidnight.toInstant().toEpochMilli() / 1000}:t>")),
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

                val buttons = mutableListOf<Button>()

                appendCouponSonhosBundleUpsellInformationIfNotNull(
                    loritta,
                    context.i18nContext,
                    claimedWebsiteCoupon,
                    "daily"
                )?.let { buttons += it }

                appendActiveReactionEventUpsellInformationIfNotNull(
                    loritta,
                    context,
                    context.i18nContext,
                    ReactionEventsAttributes.getActiveEvent(java.time.Instant.now())
                )?.let { buttons += it }

                if (buttons.isNotEmpty()) {
                    buttons.chunked(5)
                        .forEach {
                            actionRow(it)
                        }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}