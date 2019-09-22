package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.lorittaShards
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RemindersThread : Thread("Reminders Thread") {
	override fun run() {
		super.run()

		while (true) {
			try {
				checkReminders()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000)
		}
	}

	fun checkReminders() {
		val reminders = transaction(Databases.loritta) {
			Reminder.find { Reminders.remindAt.lessEq(System.currentTimeMillis()) }.toMutableList()
		}

		for (reminder in reminders) {
			val channel = lorittaShards.getTextChannelById(reminder.channelId.toString())

			if (channel != null && channel.canTalk()) {
				channel.sendMessage("<a:lori_notification:394165039227207710> | <@" + reminder.userId + "> Lembrete! `" + reminder.content + "`").queue()

				transaction(Databases.loritta) {
					Reminders.deleteWhere { Reminders.remindAt.lessEq(System.currentTimeMillis()) }
				}
			} else {
				// TODO: Enviar na DM do usuário
			}
		}
	}
}