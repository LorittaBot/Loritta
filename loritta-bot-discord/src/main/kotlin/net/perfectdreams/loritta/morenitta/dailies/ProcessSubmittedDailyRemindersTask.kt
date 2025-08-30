package net.perfectdreams.loritta.morenitta.dailies

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.MediaGallery
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyReminderNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserNotificationSettings
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.NotificationType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ProcessSubmittedDailyRemindersTask(val m: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    suspend fun processDailyReminders() {
        while (true) {
            logger.info { "Processing pending daily reminder notifications..." }

            try {
                val i18nContext = m.languageManager.defaultI18nContext

                // This is a bit annoying to make, but not impossible!
                // We don't need to worry about multiple instances trying to access this because we know that only the main cluster will process these
                val (totalPending, pendingNotifications) = m.transaction {
                    val totalPending = DailyReminderNotifications.selectAll().where {
                        DailyReminderNotifications.processedAt.isNull()
                    }.orderBy(DailyReminderNotifications.submittedAt to SortOrder.ASC)
                        .count()

                    val pendingNotifications = DailyReminderNotifications.selectAll().where {
                        DailyReminderNotifications.processedAt.isNull()
                    }.orderBy(DailyReminderNotifications.submittedAt to SortOrder.ASC)
                        .limit(5)
                        .toList()

                    return@transaction Pair(totalPending, pendingNotifications)
                }

                if (pendingNotifications.isEmpty()) {
                    logger.info { "Backing off for 5s because there aren't any pending daily reminders to be sent..." }
                    delay(5_000) // Back off for a few seconds...
                    continue
                }

                // Parallelize *everything*
                val jobs = mutableListOf<Job>()

                // We don't make everything in a single transaction to avoid things like "whoops Loritta shutdown before she was able to process everything! Every state was lost nooooo"
                for ((index, pendingNotification) in pendingNotifications.withIndex()) {
                    jobs += GlobalScope.launch {
                        logger.info { "Trying to notify user ${pendingNotification[DailyReminderNotifications.userId]} about the daily reward... ($index/${pendingNotifications.size} - total pending: $totalPending)" }

                        val userId = pendingNotification[DailyReminderNotifications.userId]
                        val todayAtMidnight = pendingNotification[DailyReminderNotifications.triggeredForDaily].atZone(Constants.LORITTA_TIMEZONE)

                        // Technically couldn't this be just "pendingNotification[DailyReminderNotifications.triggeredForDaily].toInstant()"?
                        val todayAtMidnightAsEpochMillis = todayAtMidnight.toInstant().toEpochMilli()

                        var success = false

                        try {
                            val hasTypeDisabled = m.transaction {
                                UserNotificationSettings.selectAll()
                                    .where {
                                        UserNotificationSettings.userId eq userId and (UserNotificationSettings.type eq NotificationType.DAILY_REMINDER) and (UserNotificationSettings.enabled eq false)
                                    }
                                    .count() != 0L
                            }

                            if (!hasTypeDisabled) {
                                logger.info { "User $userId has disabled the daily reminder notification type, skipping..." }
                                success = true
                            } else {
                                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(userId) ?: return@launch

                                // We'll calculate the current user streak
                                // Honestly there are better ways to do this, however they get way too complex way too quickly
                                // So we'll do this the old fashioned way:tm:
                                val allReceivedDailiesBeforeMidnight = m.transaction {
                                    Dailies.select(Dailies.receivedAt)
                                        .where {
                                            Dailies.receivedById eq userId and (Dailies.receivedAt less todayAtMidnightAsEpochMillis)
                                        }
                                        .orderBy(Dailies.receivedAt, SortOrder.DESC)
                                        .map { it[Dailies.receivedAt] }
                                }

                                // Today's date is the "last"
                                var lastDate = todayAtMidnight.toLocalDate()
                                var streak = 0

                                for (dailyTime in allReceivedDailiesBeforeMidnight) {
                                    val dailyDate = Instant.ofEpochMilli(dailyTime).atZone(Constants.LORITTA_TIMEZONE).toLocalDate()

                                    // Fail-safe for when we get two dailies on the same day (mostly useful when debugging things)
                                    // Instead of not counting, we'll just ignore it on our streak
                                    if (dailyDate == lastDate) {
                                        lastDate = dailyDate
                                        continue
                                    }

                                    if (dailyDate == lastDate.minusDays(1)) {
                                        // Yippee, we are on a streak!
                                        lastDate = dailyDate
                                        streak++
                                    } else {
                                        // Sadness, we are not on a streak anymore...
                                        break
                                    }
                                }

                                privateChannel.sendMessage(
                                    MessageCreate {
                                        this.useComponentsV2 = true

                                        this.components += Container {
                                            this.accentColor = LorittaColors.LorittaAqua.rgb

                                            +TextDisplay(
                                                buildString {
                                                    appendLine(
                                                        buildString {
                                                            append("### ${Emotes.LoriRich} ${i18nContext.get(I18nKeysData.DailyRewardReminder.Title)}")
                                                            if (streak >= 2)
                                                                append(" **_[${i18nContext.get(I18nKeysData.DailyRewardReminder.Streak(streak))} \uD83D\uDD25]_**")
                                                        }
                                                    )

                                                    for (line in i18nContext.get(
                                                        I18nKeysData.DailyRewardReminder.Description(
                                                            "<t:1490842800:t>",
                                                        )
                                                    )) {
                                                        appendLine(line)
                                                    }
                                                }
                                            )

                                            +ActionRow.of(
                                                Button.of(
                                                    ButtonStyle.LINK,
                                                    GACampaigns.dailyWebRewardDiscordCampaignUrl(
                                                        m.config.loritta.website.url,
                                                        "daily-reminder",
                                                        "dm-reminder"
                                                    ),
                                                    i18nContext.get(I18nKeysData.DailyRewardReminder.ClaimDailyReward)
                                                ).withEmoji(Emotes.Sonhos3.toJDA())
                                            )

                                            +MediaGallery {
                                                this.item("https://stuff.loritta.website/loritta-daily-yafyr.png")
                                            }

                                            +TextDisplay("-# ${i18nContext.get(I18nKeysData.DailyRewardReminder.YouReceivedThisMessageBecauseYouGotDailyRewardYesterday)} ${Emotes.LoriFlushed}")
                                        }
                                    }
                                ).await()

                                logger.info { "Successfully notified user $userId about their daily reward!" }
                                success = true
                            }
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to remind the user $userId about the daily reward!" }
                        } finally {
                            // And now we mark it as processed!
                            m.transaction {
                                DailyReminderNotifications.update({ DailyReminderNotifications.id eq pendingNotification[DailyReminderNotifications.id] }) {
                                    it[DailyReminderNotifications.processedAt] = Instant.now()
                                    it[DailyReminderNotifications.successfullySent] = success
                                }
                            }
                        }
                    }
                }

                jobs.joinAll()
            } catch (e: Throwable) {
                // This should NEVER EVER HAPPEN because if it happens, then it means that something went TERRIBLY wrong
                // This is only here to avoid the task getting stopped altogether due to some random errors
                // We do have a backoff to avoid filling our logs with that message if something goes DEEPLY wrong
                // And to avoid the CPU getting pinned at 100%
                logger.warn(e) { "Something went wrong while trying to process pending daily reminder notifications! Backing off for 5s..." }
                delay(5_000)
            }
        }
    }
}