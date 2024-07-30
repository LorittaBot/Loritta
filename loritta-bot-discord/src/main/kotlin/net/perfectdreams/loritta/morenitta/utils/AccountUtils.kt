package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Daily
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant

object AccountUtils {
    /**
     * Gets the user's last received daily reward
     *
     * @param profile   the user's profile
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(loritta: LorittaBot, profile: Profile, afterTime: Long = Long.MIN_VALUE): Daily? {
        return getUserLastDailyRewardReceived(loritta, profile.id.value, afterTime)
    }

    /**
     * Gets the user's last received daily reward
     *
     * @param profile   the user's profile
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(loritta: LorittaBot, userId: Long, afterTime: Long = Long.MIN_VALUE): Daily? {
        return loritta.newSuspendedTransaction {
            val dailyResult = Dailies.select {
                Dailies.receivedById eq userId and (Dailies.receivedAt greaterEq afterTime)
            }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .firstOrNull()

            if (dailyResult != null)
                Daily.wrapRow(dailyResult)
            else null
        }
    }

    /**
     * Gets the user's received daily reward from today, or null, if the user didn't get the daily reward today
     *
     * @param profile the user's profile
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserTodayDailyReward(loritta: LorittaBot, profile: Profile) = getUserTodayDailyReward(loritta, profile.id.value)

    /**
     * Gets the user's received daily reward from today, or null, if the user didn't get the daily reward today
     *
     * @param profile the user's profile
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserTodayDailyReward(loritta: LorittaBot, userId: Long) = getUserDailyRewardInTheLastXDays(loritta, userId, 0)

    /**
     * Gets the user's received daily reward from the last [dailyInThePreviousDays] days, or null, if the user didn't get the daily reward in the specified threshold
     *
     * @param profile the user's profile
     * @param dailyInThePreviousDays the daily minimum days threshold
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserDailyRewardInTheLastXDays(loritta: LorittaBot, profile: Profile, dailyInThePreviousDays: Long): Daily? {
        return getUserDailyRewardInTheLastXDays(loritta, profile.id.value, dailyInThePreviousDays)
    }

    /**
     * Gets the user's received daily reward from the last [dailyInThePreviousDays] days, or null, if the user didn't get the daily reward in the specified threshold
     *
     * @param profile the user's profile
     * @param dailyInThePreviousDays the daily minimum days threshold
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserDailyRewardInTheLastXDays(loritta: LorittaBot, userId: Long, dailyInThePreviousDays: Long): Daily? {
        val dayAtMidnight = Instant.now()
            .atZone(Constants.LORITTA_TIMEZONE)
            .toOffsetDateTime()
            .minusDays(dailyInThePreviousDays)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toInstant()
            .toEpochMilli()

        return getUserLastDailyRewardReceived(loritta, userId, dayAtMidnight)
    }

    suspend fun checkAndSendMessageIfUserIsBanned(loritta: LorittaBot, context: UnleashedContext, user: User)
            = checkAndSendMessageIfUserIsBanned(loritta, context, user.idLong)

    suspend fun checkAndSendMessageIfUserIsBanned(loritta: LorittaBot, context: UnleashedContext, userId: Long): Boolean {
        // Check if the user is banned from using Loritta
        val userBannedState = loritta.pudding.users.getUserBannedState(UserId(userId))

        if (userBannedState != null) {
            val banDateInEpochMillis = userBannedState.bannedAt.toEpochMilliseconds()
            val expiresDateInEpochMillis = userBannedState.expiresAt?.toEpochMilliseconds()

            val messageBuilder: InlineMessage<*>.() -> (Unit) = {
                apply(
                    buildBanMessage(
                        context.i18nContext,
                        context.user.isLorittaSupervisor(loritta.lorittaShards),
                        userId,
                        userBannedState.bannedBy?.value?.toLong(),
                        userBannedState.reason,
                        banDateInEpochMillis,
                        expiresDateInEpochMillis
                    )
                )
            }

            context.reply(context.wasInitiallyDeferredEphemerally ?: true) {
                messageBuilder()
            }
            return true
        }

        return false
    }

    suspend fun checkAndSendMessageIfUserIsBanned(context: CommandContext, userProfile: Profile): Boolean {
        val bannedState = userProfile.getBannedState(context.loritta)

        if (bannedState != null) {
            context.sendMessage(
                MessageCreate {
                    apply(
                        buildBanMessage(
                            context.i18nContext,
                            context.userHandle.isLorittaSupervisor(context.loritta.lorittaShards),
                            userProfile.userId,
                            bannedState[BannedUsers.bannedBy],
                            bannedState[BannedUsers.reason],
                            bannedState[BannedUsers.bannedAt],
                            bannedState[BannedUsers.expiresAt]
                        )
                    )
                }
            )
            return true
        }
        return false
    }

    private fun buildBanMessage(
        i18nContext: I18nContext,
        showWhoBannedTheUser: Boolean,
        userId: Long,
        bannedById: Long?,
        reason: String,
        banDateInEpochMillis: Long,
        expiresDateInEpochMillis: Long?,
    ): InlineMessage<*>.() -> (Unit) = {
        if (expiresDateInEpochMillis == null) {
            styled(
                i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.UserIsBannedPermanent("<@$userId>")),
                Emotes.LoriBonk
            )
        } else {
            styled(
                i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.UserIsBannedTemporary("<@$userId>")),
                Emotes.LoriBonk
            )
        }

        styled(
            i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.Reason),
            Emotes.LoriReading
        )

        styled(reason)

        styled(
            i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.BannedAt(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(banDateInEpochMillis))),
            Emotes.LoriHmpf
        )

        if (expiresDateInEpochMillis == null) {
            styled(
                i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.ExpiresAtPermanent),
                Emotes.LoriLurk
            )
        } else {
            styled(
                i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.ExpiresAtTemporary(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(expiresDateInEpochMillis))),
                Emotes.LoriLurk
            )
        }

        if (showWhoBannedTheUser && bannedById != null) {
            styled(
                i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.BannedBy("<@$bannedById>")),
                Emotes.LoriBanHammer
            )
        }

        styled(
            i18nContext.get(I18nKeysData.Commands.UserIsLorittaBanned.TooLateToSaySorry),
            Emotes.LoriSob
        )
    }
}