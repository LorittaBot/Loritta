package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.convertToEpochMillis
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.onResponseByAuthor
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import com.mrpowergamerbr.loritta.utils.save
import java.util.*


class LembrarCommand : AbstractCommand("lembrar", listOf("lembre", "remind", "remindme", "lembrete", "reminder"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "tempo mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["LEMBRAR_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("1 minuto dar comida para o dog");
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var message = context.strippedArgs.joinToString(separator = " ")

			val reply = context.reply(
					LoriReply(
							message = locale["LEMBRAR_SetHour"],
							prefix = "⏰"
					)
			)

			reply.onResponseByAuthor(context, {
				loritta.messageInteractionCache.remove(reply.id)
				reply.delete().queue()
				val inMillis = it.message.contentDisplay.convertToEpochMillis()
				val calendar = Calendar.getInstance()
				calendar.timeInMillis = inMillis

				// Criar o Lembrete
				var reminder = Reminder(context.guild.id, context.message.textChannel.id, inMillis, message.trim());
				var profile = context.lorittaUser.profile

				profile.reminders.add(reminder);

				loritta save profile

				val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
				val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
				val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
				val minutes = String.format("%02d", calendar[Calendar.MINUTE])
				context.sendMessage(context.getAsMention(true) + locale["LEMBRAR_SUCCESS", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes])
			})

			reply.onReactionAddByAuthor(context, {
				loritta.messageInteractionCache.remove(reply.id)
				reply.delete().queue()
				context.reply(
						LoriReply(
								message = locale["LEMBRAR_Cancelado"],
								prefix = "\uD83D\uDDD1"
						)
				)
			})

			reply.addReaction("\uD83D\uDE45").complete()
		} else {
			this.explain(context);
		}
	}
}