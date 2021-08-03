package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.TimeUtils
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.onResponseByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/*
* Thread to check user reminders
* */
class RemindersThread : Thread("Reminders Thread") {
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
        val reminders = transaction(Databases.loritta) {
            Reminder.find { Reminders.remindAt.lessEq(System.currentTimeMillis()) }
                    .toList()
        }

        val notifiedReminders = mutableListOf<Reminder>()

        for (reminder in reminders) {
            if (reminder.id.value !in remindersThatFailedToDelete) {
                try {
                    val channel = lorittaShards.getTextChannelById(reminder.channelId.toString())

                    val reminderText = "<a:lori_notification:394165039227207710> **|** <@${reminder.userId}> Reminder! `${reminder.content.stripCodeMarks().escapeMentions().substringIfNeeded(0..1000)}`\n" +
                            "🔹 **|** Click $SNOOZE_EMOTE to snooze for $DEFAULT_SNOOZE_MINUTES minutes, or click $SCHEDULE_EMOTE to choose how long to snooze."

                    if (channel != null && channel.canTalk()) {
                        channel.sendMessage(
                                MessageBuilder(reminderText)
                                        .allowMentions(Message.MentionType.USER, Message.MentionType.EMOTE)
                                        .build()
                        ).queue {
                            addSnoozeListener(it, reminder)
                        }

                        notifiedReminders += reminder
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to notify ${reminder.userId} about ${reminder.content} at channel ${reminder.channelId}" }
                }
            } else {
                notifiedReminders += reminder
            }
        }

        // Apenas delete os lembretes NOTIFICADOS, as vezes lembretes podem ser de canais em outros clusters, e a gente não deve deletá-los
        try {
            transaction(Databases.loritta) {
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

    private fun addSnoozeListener(message: Message, reminder: Reminder) {
        if (!message.isFromGuild)
            return

        message.onReactionAddByAuthor(reminder.userId) {
            if (it.reactionEmote.isEmote(SNOOZE_EMOTE)) {
                loritta.messageInteractionCache.remove(message.idLong)

                val newReminderTime = Calendar.getInstance().timeInMillis + (Constants.ONE_MINUTE_IN_MILLISECONDS * DEFAULT_SNOOZE_MINUTES)
                loritta.newSuspendedTransaction {
                    Reminder.new {
                        userId = reminder.userId
                        channelId = reminder.channelId
                        remindAt = newReminderTime
                        content = reminder.content
                    }
                }

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = newReminderTime

                val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
                val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
                val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
                val minutes = String.format("%02d", calendar[Calendar.MINUTE])
                val messageContent = loritta.localeManager.getLocaleById("default")["commands.command.remindme.success", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes]

                message.editMessage("<@${reminder.userId}> $messageContent").queue()
                message.clearReactions().queue()
            }

            if (it.reactionEmote.isEmote(SCHEDULE_EMOTE)) {
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
        reply.onResponseByAuthor(reminder.userId, originalMessage.guild.idLong, reminder.channelId) {
            loritta.messageInteractionCache.remove(reply.idLong)
            loritta.messageInteractionCache.remove(originalMessage.idLong)
            reply.delete().queue()

            val inMillis = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)

            loritta.newSuspendedTransaction {
                Reminder.new {
                    userId = reminder.userId
                    channelId = reminder.channelId
                    remindAt = inMillis
                    content = reminder.content
                }
            }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = inMillis

            val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
            val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
            val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
            val minutes = String.format("%02d", calendar[Calendar.MINUTE])
            val messageContent = loritta.localeManager.getLocaleById("default")["commands.command.remindme.success", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes]

            reply.channel.sendMessage("<@${reminder.userId}> $messageContent").queue()
        }

        reply.onReactionAddByAuthor(reminder.userId) {
            if (it.reactionEmote.isEmote(CANCEL_EMOTE)) {
                loritta.messageInteractionCache.remove(reply.idLong)
                reply.delete().queue()
                reply.channel.sendMessage("\uD83D\uDDD1️| <@${reminder.userId}> Reminder cancelled!").queue()
            }
        }
        reply.addReaction(CANCEL_EMOTE).queue()
    }
}