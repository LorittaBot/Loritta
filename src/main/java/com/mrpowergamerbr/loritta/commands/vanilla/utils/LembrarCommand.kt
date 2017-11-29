package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
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


class LembrarCommand : CommandBase("lembrar") {
	companion object {
		val TIME_PATTERN = "(([01]\\d|2[0-3]):([0-5]\\d)(:([0-5]\\d))?) ?(am|pm)?".toPattern()
		val DATE_PATTERN = "(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.]([0-9]+)".toPattern()
	}

	override fun getAliases(): List<String> {
		return listOf("lembre", "remind", "remindme");
	}

	override fun getUsage(): String {
		return "tempo mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.LEMBRAR_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("1 minuto dar comida para o dog");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var message = context.strippedArgs.joinToString(separator = " ")
			val metadata = context.metadata

			val reply = context.reply(
					LoriReply(
							message = locale["LEMBRAR_SetHour"],
							prefix = "⏰"
					)
			)

			reply.onResponseByAuthor(context, {
				loritta.messageInteractionCache.remove(reply.id)
				val inMillis = it.message.content.convertToEpochMillis()
				val calendar = Calendar.getInstance()
				calendar.timeInMillis = inMillis

				// Criar o Lembrete
				var reminder = Reminder(context.guild.id, context.message.textChannel.id, inMillis, message.trim());
				var profile = context.lorittaUser.profile

				profile.reminders.add(reminder);

				loritta save profile

				context.sendMessage(context.getAsMention(true) + locale["LEMBRAR_SUCCESS", calendar[Calendar.DAY_OF_MONTH], calendar[Calendar.MONTH] + 1, calendar[Calendar.YEAR], calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE]])
			})

			reply.onReactionAddByAuthor(context, {
				loritta.messageInteractionCache.remove(reply.id)
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