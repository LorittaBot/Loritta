package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import com.mrpowergamerbr.loritta.utils.save

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RemindersThread : Thread("Reminders Thread") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkReminders();
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000);
		}
	}

	fun checkReminders() {
		val list = loritta.usersColl.find(
				Filters.gt("reminders", listOf<Any>())
		).iterator()

		list.use {
			while (it.hasNext()) {
				val profile = it.next()

				val toRemove = mutableListOf<Reminder>()

				for (reminder in profile.reminders) {
					if (System.currentTimeMillis() >= reminder.remindMe) {
						toRemove.add(reminder);

						if (reminder.guild == null)
							continue

						val guild = lorittaShards.getGuildById(reminder.guild!!)

						if (guild != null) {
							val textChannel = guild.getTextChannelById(reminder.textChannel) ?: return

							textChannel.sendMessage("<a:lori_notification:394165039227207710> | <@" + profile.userId + "> Lembrete! `" + reminder.reason + "`").complete();
						}
					}
				}

				if (!toRemove.isEmpty()) {
					profile.reminders.removeAll(toRemove)
					loritta save profile
				}
			}
		}
	}
}