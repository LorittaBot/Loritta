package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RemindersThread : Thread("Reminders Thread") {
	companion object {
		private val logger = KotlinLogging.logger {}
		private const val SNOOZE_EMOTE = "\uD83D\uDCA4"
		private const val SNOOZE_MINUTES = 10
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				checkReminders()
			} catch (e: Exception) {
				logger.warn(e) { "Something went wrong while checking reminders!"}
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

				if (channel != null && channel.canTalk()) {
					channel.sendMessage("<a:lori_notification:394165039227207710> | <@" + reminder.userId + "> Lembrete! `" + reminder.content.substringIfNeeded(0..1000) + "`").queue {
						addSnoozeListener(it, reminder)
					}
				} else {
					val user = lorittaShards.getUserById(reminder.userId) ?: return

					user.openPrivateChannel().queue { privateChannel ->
						privateChannel.sendMessage("<a:lori_notification:394165039227207710> | <@" + reminder.userId + "> Lembrete! `" + reminder.content + "`").queue {
							addSnoozeListener(it, reminder)
						}
					}
				}
				notifiedReminders.add(reminder)
			} catch (e: Throwable) {
				logger.warn(e) { "Something went wrong while trying to notify ${reminder.userId} about ${reminder.content} at channel ${reminder.channelId}" }
			}
		}

		// Apenas delete os lembretes NOTIFICADOS, as vezes lembretes podem ser de canais em outros clusters, e a gente não deve deletá-los
		transaction(Databases.loritta) {
			Reminders.deleteWhere { Reminders.id inList notifiedReminders.map { it.id } }
		}
	}

	private fun addSnoozeListener(message: Message, reminder: Reminder) {
		message.onReactionAddByAuthor(reminder.userId) {
			if (it.reactionEmote.isEmote(SNOOZE_EMOTE)) {

				loritta.newSuspendedTransaction {
					Reminder.new {
						userId = reminder.userId
						channelId = reminder.channelId
						remindAt = reminder.remindAt + 60_000 * SNOOZE_MINUTES
						content = reminder.content
					}
				}

				message.editMessage("<@${reminder.userId}> | I will remind you again in **$SNOOZE_MINUTES** minutes!").queue()
				if (message.isFromGuild) {
					message.clearReactions().queue()
				}

			}
		}
		message.addReaction(SNOOZE_EMOTE).queue()
	}
}