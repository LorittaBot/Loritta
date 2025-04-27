package net.perfectdreams.loritta.morenitta.threads

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reminders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Reminder
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

/*
* Thread to check user reminders
* */
class RemindersThread(val loritta: LorittaBot) : Thread("Reminders Thread") {
    private val remindersThatFailedToDelete = mutableSetOf<Long>()

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val SNOOZE_EMOTE = "\uD83D\uDCA4"
        private const val SCHEDULE_EMOTE = "\uD83D\uDCC5"
        private const val CANCEL_EMOTE = "\uD83D\uDE45"
        private const val DEFAULT_SNOOZE_MINUTES = 10
    }

    override fun run() {
        super.run()

        while (true) {
            try {
                checkReminders()
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while checking reminders!" }
            }
            sleep(5000)
        }
    }

    private fun checkReminders() {
        runBlocking {
            val reminders = loritta.pudding.transaction {
                Reminder.find { Reminders.remindAt.lessEq(System.currentTimeMillis()) }
                    .toList()
            }

            val notifiedReminders = mutableListOf<Reminder>()

            logger.info { "Retrieved ${reminders.size} reminders from the database!" }

            for (reminder in reminders) {
                if (reminder.id.value !in remindersThatFailedToDelete) {
                    try {
                        val guildId = reminder.guildId // Not all reminders has the guild ID set! This is a new field :3
                        val channel = if (guildId != null) {
                            // ...buuuuut if it ain't null, it is better for us! Because we avoid multiple cache hits if this reminder is NOT for us
                            // The advantage is that...
                            // If the guild is present but the channel doesn't exist, we do (guild cache + all channels cache)
                            // If the guild is present and it is a text channel, we do (guild cache + one channel cache)
                            // If the guild ID is not present, we do (all channels cache)
                            val guild = loritta.lorittaShards.getGuildById(guildId) ?: continue // Not for us, or guild isn't loaded yet, skip!

                            // Now that we know that the guild exists, we will attmept to get the channel
                            guild.getGuildMessageChannelById(reminder.channelId)
                        } else {
                            // Get the channel directly if we don't know the guild
                            loritta.lorittaShards.getGuildMessageChannelById(reminder.channelId.toString())
                        }

                        val reminderText = "<a:lori_notification:394165039227207710> **|** <@${reminder.userId}> Reminder! `${
                            reminder.content.stripCodeMarks().escapeMentions().substringIfNeeded(0..1000)
                        }`\n" +
                                "🔹 **|** Click $SNOOZE_EMOTE to snooze for $DEFAULT_SNOOZE_MINUTES minutes, or click $SCHEDULE_EMOTE to choose how long to snooze."

                        if (channel != null && channel.canTalk()) {
                            channel.sendMessage(
                                MessageCreateBuilder()
                                    .setContent(reminderText)
                                    .setAllowedMentions(setOf(Message.MentionType.USER, Message.MentionType.EMOJI))
                                    .build()
                            ).queue {
                                addSnoozeListener(it, reminder)
                            }

                            notifiedReminders += reminder
                        } else if (System.currentTimeMillis() - reminder.remindAt >= Constants.ONE_WEEK_IN_MILLISECONDS) {
                            // Look, I will be honest with you
                            // If we couldn't find the channel of this reminder after 1 week, the channel was probably deleted (or I can't talk in it!) and we should consider that we notified the user
                            logger.warn { "We weren't able to notify ${reminder.id} (user ID: ${reminder.userId}, channel ID: ${reminder.channelId}, guild ID: ${reminder.guildId}) for a long time, so we will pretend that this notification was notified and remove it..." }
                            notifiedReminders += reminder
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to notify ${reminder.userId} about ${reminder.content} at channel ${reminder.channelId}" }

                        if (System.currentTimeMillis() - reminder.remindAt  >= Constants.ONE_WEEK_IN_MILLISECONDS) {
                            // Look, I will be honest with you
                            // If we couldn't find the channel of this reminder after 1 week, the channel was probably deleted (or I can't talk in it!) and we should consider that we notified the user
                            logger.warn { "We weren't able to notify ${reminder.id} (user ID: ${reminder.userId}, channel ID: ${reminder.channelId}, guild ID: ${reminder.guildId}) for a long time, so we will pretend that this notification was notified and remove it..." }
                            notifiedReminders += reminder
                        }
                    }
                } else {
                    notifiedReminders += reminder
                }
            }

            // Apenas delete os lembretes NOTIFICADOS, as vezes lembretes podem ser de canais em outros clusters, e a gente não deve deletá-los
            try {
                loritta.pudding.transaction {
                    Reminders.deleteWhere { Reminders.id inList notifiedReminders.map { it.id } }
                }
                notifiedReminders.intersect(remindersThatFailedToDelete).forEach(remindersThatFailedToDelete::remove)
            } catch (e: Exception) {
                logger.debug(e) { "Could not delete the notified reminders from database." }
                // Failed to delete reminders from database.
                // If this ever happen, we should store in memory the reminders that failed to delete
                // because we don't want to spam servers
                notifiedReminders.map { it.id.value }.forEach(remindersThatFailedToDelete::add)
            }
        }
    }

    private fun addSnoozeListener(message: Message, reminder: Reminder) {
        if (!message.isFromGuild)
            return

        message.onReactionAddByAuthor(loritta, reminder.userId) {
            if (it.emoji.isEmote(SNOOZE_EMOTE)) {
                loritta.messageInteractionCache.remove(message.idLong)

                val newReminderTime = Calendar.getInstance(TimeZone.getTimeZone(Constants.LORITTA_TIMEZONE)).timeInMillis + (Constants.ONE_MINUTE_IN_MILLISECONDS * DEFAULT_SNOOZE_MINUTES)
                loritta.newSuspendedTransaction {
                    Reminder.new {
                        userId = reminder.userId
                        guildId = reminder.guildId
                        channelId = reminder.channelId
                        remindAt = newReminderTime
                        content = reminder.content
                    }
                }

                val calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.LORITTA_TIMEZONE))
                calendar.timeInMillis = newReminderTime

                val i18nContext = loritta.languageManager.defaultI18nContext
                val messageContent = i18nContext.get(I18nKeysData.Commands.Command.Reminder.Create.Success(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(newReminderTime)))

                message.editMessage("<@${reminder.userId}> $messageContent").queue()
                message.clearReactions().queue()
            }

            if (it.emoji.isEmote(SCHEDULE_EMOTE)) {
                val remindStr = "$SCHEDULE_EMOTE | <@${reminder.userId}> When do you want me to remind you again? (`1 hour`, `5 minutes`, `12:00 11/08/2018`, etc)"
                message.channel.sendMessage(remindStr).queue { reply ->
                    awaitSchedule(reply, message, reminder)
                }
                if (message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE))
                    it.user?.let { user -> it.reaction.removeReaction(user).queue() }
            }

        }
        message.addReaction(SNOOZE_EMOTE).queue()
        message.addReaction(SCHEDULE_EMOTE).queue()
    }

    private fun awaitSchedule(reply: Message, originalMessage: Message, reminder: Reminder) {
        reply.onResponseByAuthor(loritta, reminder.userId, originalMessage.guild.idLong, reminder.channelId) {
            loritta.messageInteractionCache.remove(reply.idLong)
            loritta.messageInteractionCache.remove(originalMessage.idLong)
            reply.delete().queue()

            val inMillis = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)

            loritta.newSuspendedTransaction {
                Reminder.new {
                    userId = reminder.userId
                    guildId = reminder.guildId
                    channelId = reminder.channelId
                    remindAt = inMillis
                    content = reminder.content
                }
            }

            val calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.LORITTA_TIMEZONE))
            calendar.timeInMillis = inMillis

            val i18nContext = loritta.languageManager.defaultI18nContext
            val messageContent = i18nContext.get(I18nKeysData.Commands.Command.Reminder.Create.Success(DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(calendar.toInstant())))

            reply.channel.sendMessage("<@${reminder.userId}> $messageContent").queue()
        }

        reply.onReactionAddByAuthor(loritta, reminder.userId) {
            if (it.emoji.isEmote(CANCEL_EMOTE)) {
                loritta.messageInteractionCache.remove(reply.idLong)
                reply.delete().queue()
                reply.channel.sendMessage("\uD83D\uDDD1️| <@${reminder.userId}> Reminder cancelled!").queue()
            }
        }
        reply.addReaction(CANCEL_EMOTE).queue()
    }
}