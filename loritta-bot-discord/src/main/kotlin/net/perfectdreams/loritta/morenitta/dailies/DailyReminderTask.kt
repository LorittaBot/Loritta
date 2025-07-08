package net.perfectdreams.loritta.morenitta.dailies

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.MediaGallery
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.MarriageCommand
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Container
import java.time.Instant
import java.time.ZonedDateTime

class DailyReminderTask(val m: LorittaBot) : NamedRunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override val taskName = "daily-reminder-task"

    override suspend fun run() {
        val i18nContext = m.languageManager.defaultI18nContext

        val today = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
        val todayAtMidnight = today
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayOneDayAgo = today
            .minusDays(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayAtMidnightAsEpochMillis = todayAtMidnight.toInstant().toEpochMilli()
        val todayOneDayAgoAsEpochMillis = todayOneDayAgo.toInstant().toEpochMilli()

        val usersThatGotDailyYesterday = m.pudding.transaction(repetitions = Int.MAX_VALUE) {
            // Update the task timer
            updateStoredTimer(m)

            Dailies.select(Dailies.receivedById)
                .where {
                    Dailies.receivedAt greaterEq todayOneDayAgoAsEpochMillis and (Dailies.receivedAt.less(todayAtMidnightAsEpochMillis))
                }
                .map { it[Dailies.receivedById] }
                .distinct() // This technically is not needed BUT who knows right?
        }

        logger.info { "There are ${usersThatGotDailyYesterday.size} users that will be reminded about their daily reward!" }
        for ((index, userId) in usersThatGotDailyYesterday.withIndex()) {
            logger.info { "Trying to notify user $userId about the daily reward... ($index/${usersThatGotDailyYesterday.size})" }
            try {
                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(userId) ?: continue

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
                                                append(" **_[SEQUÊNCIA ${streak}X \uD83D\uDD25]_**")
                                        }
                                    )

                                    for (line in i18nContext.get(
                                        I18nKeysData.DailyRewardReminder.Description(
                                            "<t:1490842800:t>",
                                            GACampaigns.dailyWebRewardDiscordCampaignUrl(
                                                m.config.loritta.website.url,
                                                "daily-reminder",
                                                "dm-reminder"
                                            )
                                        )
                                    )) {
                                        appendLine(line)
                                    }
                                }
                            )

                            +MediaGallery {
                                this.item("http://stuff.loritta.website/loritta-daily-yafyr.png")
                            }
                        }
                    }
                ).await()

                logger.info { "Successfully notified user $userId about their daily reward!" }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to remind the user $userId about the daily reward!" }
            }
        }
    }
}
