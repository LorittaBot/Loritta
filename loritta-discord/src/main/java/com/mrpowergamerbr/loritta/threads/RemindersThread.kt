package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.MarkdownUtil
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RemindersThread : Thread("Reminders Thread") {
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
				logger.warn(e) {"Something went wrong while checking reminders!"}
			}
			sleep(5000)
		}
	}

	fun checkReminders() {
		val reminders = transaction(Databases.loritta) {
			Reminder.find { Reminders.remindAt.lessEq(System.currentTimeMillis()) }
					.toList()
		}

		val notifiedReminders = mutableListOf<Reminder>()

		for (reminder in reminders) {
			try {
				val channel = lorittaShards.getTextChannelById(reminder.channelId.toString())

				val reminderText = "<a:lori_notification:394165039227207710> **|** <@${reminder.userId}> Reminder! `${reminder.content.substringIfNeeded(0..1000)}`\n" +
						"🔹 **|** Click $SNOOZE_EMOTE to snooze for $DEFAULT_SNOOZE_MINUTES minutes, or click $SCHEDULE_EMOTE to choose how long to snooze."

				if (channel != null && channel.canTalk()) {
					channel.sendMessage(reminderText).queue {
						addSnoozeListener(it, reminder)
					}

					notifiedReminders += reminder
				}
			} catch (e: Exception) {
				logger.warn(e) { "Something went wrong while trying to notify ${reminder.userId} about ${reminder.content} at channel ${reminder.channelId}" }
			}
		}

		// Apenas delete os lembretes NOTIFICADOS, as vezes lembretes podem ser de canais em outros clusters, e a gente não deve deletá-los
		transaction(Databases.loritta) {
			Reminders.deleteWhere { Reminders.id inList notifiedReminders.map { it.id } }
		}
	}

	private fun addSnoozeListener(message: Message, reminder: Reminder) {
		if (!message.isFromGuild)
			return

		message.onReactionAddByAuthor(reminder.userId) {

			if (it.reactionEmote.isEmote(SNOOZE_EMOTE)) {
				loritta.messageInteractionCache.remove(message.idLong)
				loritta.newSuspendedTransaction {
					Reminder.new {
						userId = reminder.userId
						channelId = reminder.channelId
						remindAt = Calendar.getInstance().timeInMillis + Constants.ONE_MINUTE_IN_MILLISECONDS * DEFAULT_SNOOZE_MINUTES
						content = reminder.content
					}
				}

				message.editMessage("<@${reminder.userId}> I will remind you again in **$DEFAULT_SNOOZE_MINUTES minutes**!").queue()
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
			val remindIn = MarkdownUtil.bold(MarkdownUtil.monospace(it.message.contentDisplay))
			reply.channel.sendMessage("<@${reminder.userId}> I will remind you again in $remindIn!").queue()
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