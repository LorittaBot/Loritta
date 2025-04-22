package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.EconomyState
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.selectAll
import java.time.*
import java.util.*

object SonhosUtils {
    private val UPSELL_LORICOOLCARDS_AFTER = ZonedDateTime.of(2024, 11, 1, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE)
    private val UPSELL_LORICOOLCARDS_AFTER_INSTANT = UPSELL_LORICOOLCARDS_AFTER.toInstant()

    val DISABLED_ECONOMY_ID = UUID.fromString("3da6d95b-edb4-4ae9-aa56-4b13e91f3844")

    val HANGLOOSE_EMOTES = listOf(
        Emotes.LoriHanglooseRight,
        Emotes.GabrielaHanglooseRight,
        Emotes.PantufaHanglooseRight,
        Emotes.PowerHanglooseRight
    )

    fun insufficientSonhos(profile: PuddingUserProfile?, howMuch: Long) = insufficientSonhos(profile?.money ?: 0L, howMuch)
    fun insufficientSonhos(sonhos: Long, howMuch: Long) = I18nKeysData.Commands.InsufficientFunds(howMuch, howMuch - sonhos)

    fun InlineMessage<*>.appendCouponSonhosBundleUpsellInformationIfNotNull(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        activeCoupon: ClaimedWebsiteCoupon?,
        upsellMedium: String
    ): Button? {
        if (activeCoupon != null && activeCoupon.hasRemainingUses) {
            val maxUses = activeCoupon.maxUses
            if (maxUses != null) {
                styled(
                    i18nContext.get(
                        I18nKeysData.Commands.SonhosShopCouponCodeWithMaxUsesUpsell(
                            TimeFormat.DATE_TIME_SHORT.format(activeCoupon.endsAt),
                            maxUses,
                            activeCoupon.code,
                            activeCoupon.discount
                        )
                    ),
                    Emotes.LoriLurk
                )
            } else {
                styled(
                    i18nContext.get(
                        I18nKeysData.Commands.SonhosShopCouponCodeUpsell(
                            TimeFormat.DATE_TIME_SHORT.format(activeCoupon.endsAt),
                            activeCoupon.code,
                            activeCoupon.discount
                        )
                    ),
                    Emotes.LoriLurk
                )
            }

            return Button.of(
                ButtonStyle.LINK,
                GACampaigns.sonhosBundlesUpsellUrl("https://loritta.website/", "discord", upsellMedium, "sonhos-bundles-upsell", "coupon-code"),
                i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.Title)
            ).withEmoji(Emotes.Sonhos3.toJDA())
        } else {
            return null
        }
    }

    fun InlineMessage<*>.appendActiveReactionEventUpsellInformationIfNotNull(
        loritta: LorittaBot,
        context: UnleashedContext,
        i18nContext: I18nContext,
        activeReactionEvent: ReactionEvent?
    ): Button? {
        if (activeReactionEvent != null) {
            val emoji = loritta.emojiManager.get(activeReactionEvent.createCraftItemButtonMessage(i18nContext).emoji)

            return loritta.interactivityManager.button(
                context.alwaysEphemeral,
                ButtonStyle.SECONDARY,
                i18nContext.get(I18nKeysData.Commands.ActiveReactionEvent.ButtonLabel(activeReactionEvent.createEventTitle(i18nContext))),
                {
                    this.loriEmoji = emoji
                }
            ) { context ->
                val userSonhos = activeReactionEvent.rewards.filterIsInstance<ReactionEventReward.SonhosReward>().sumOf { it.sonhos }

                context.reply(true) {
                    styled(
                        i18nContext.get(
                            I18nKeysData.Commands.ActiveReactionEvent.EventUpsell(
                                activeReactionEvent.createEventTitle(i18nContext),
                                getSonhosEmojiOfQuantity(userSonhos),
                                userSonhos,
                                TimeFormat.DATE_TIME_SHORT.format(activeReactionEvent.endsAt),
                                loritta.commandMentions.eventJoin,
                                loritta.commandMentions.eventStats,
                                loritta.commandMentions.eventInventory
                            )
                        ),
                        emoji
                    )
                }
            }
        } else {
            return null
        }
    }

    suspend fun InlineMessage<*>.appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
        loritta: LorittaBot,
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

        val todayDailyReward = loritta.pudding.sonhos.getUserLastDailyRewardReceived(
            userId,
            now.toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
        )

        if (todayDailyReward != null) {
            // Already got their daily reward today, show our sonhos bundles!
            styled(
                i18nContext.get(
                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                        loritta.config.loritta.website.url,
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

    suspend fun sendEphemeralMessageIfUserHaventGotDailyRewardToday(
        loritta: LorittaBot,
        context: UnleashedContext,
        userId: UserId
    ) {
        // TODO: Do not hardcode the timezone
        val now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayDailyReward = loritta.pudding.sonhos.getUserLastDailyRewardReceived(
            userId,
            now.toKotlinLocalDateTime().toInstant(TimeZone.of("America/Sao_Paulo"))
        )

        if (todayDailyReward != null)
            return

        context.reply(true) {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.WantingMoreSonhosDaily(loritta.commandMentions.daily)),
                Emotes.Gift
            )
        }
    }

    fun getSonhosEmojiOfQuantity(quantity: Long) = when {
        quantity >= 1_000_000_000 -> Emotes.Sonhos6
        quantity >= 10_000_000 -> Emotes.Sonhos5
        quantity >= 1_000_000 -> Emotes.Sonhos4
        quantity >= 100_000 -> Emotes.Sonhos3
        quantity >= 10_000 -> Emotes.Sonhos2
        else -> Emotes.Sonhos1
    }

    suspend fun isEconomyDisabled(loritta: LorittaBot): Boolean {
        return loritta.transaction {
            EconomyState.selectAll().where {
                EconomyState.id eq DISABLED_ECONOMY_ID
            }.count() == 1L
        }
    }

    suspend fun checkIfEconomyIsDisabled(context: DiscordCommandContext) = checkIfEconomyIsDisabled(CommandContextCompat.LegacyDiscordCommandContextCompat(context))

    suspend fun checkIfEconomyIsDisabled(context: UnleashedContext): Boolean {
        if (isEconomyDisabled(context.loritta)) {
            context.reply(context.wasInitiallyDeferredEphemerally ?: true) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.EconomyIsDisabled),
                    Emotes.LoriSob
                )
            }
            return true
        }
        return false
    }

    suspend fun checkIfEconomyIsDisabled(context: CommandContextCompat): Boolean {
        if (isEconomyDisabled(context.loritta)) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.EconomyIsDisabled),
                    Emotes.LoriSob
                )
            }
            return true
        }
        return false
    }

    fun getSpecialTotalCoinFlipReward(guild: Guild?, currentTax: Double): SpecialTotalCoinFlipReward {
        // No need to change
        if (currentTax == 1.0)
            return SpecialTotalCoinFlipReward.NoChange(currentTax)

        if (guild?.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
            val today = LocalDate.now(Constants.LORITTA_TIMEZONE)
            return if (today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY) {
                // No tax during weekends poggies!!!
                SpecialTotalCoinFlipReward.LorittaCommunity(1.0, true)
            } else {
                // 2.5% any other day
                SpecialTotalCoinFlipReward.LorittaCommunity(0.975, false)
            }
        }

        if (guild?.idLong == 1204104683380285520L) {
            val today = LocalDate.now(Constants.LORITTA_TIMEZONE)
            return if (today.dayOfWeek == DayOfWeek.FRIDAY || today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY) {
                // No tax during weekends poggies!!!
                SpecialTotalCoinFlipReward.PremiumCommunity(1.0, true)
            } else {
                // 2.5% any other day
                SpecialTotalCoinFlipReward.PremiumCommunity(0.975, false)
            }
        }

        return SpecialTotalCoinFlipReward.NoChange(currentTax)
    }

    sealed class SpecialTotalCoinFlipReward(val value: Double) {
        class LorittaCommunity(value: Double, val isWeekend: Boolean) : SpecialTotalCoinFlipReward(value)
        class PremiumCommunity(value: Double, val isSpecialDay: Boolean) : SpecialTotalCoinFlipReward(value)
        class NoChange(value: Double) : SpecialTotalCoinFlipReward(value)
    }
}