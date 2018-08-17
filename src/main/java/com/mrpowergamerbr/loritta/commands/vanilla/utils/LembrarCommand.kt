package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import net.dv8tion.jda.core.EmbedBuilder
import org.bson.Document
import java.awt.Color
import java.util.*

class LembrarCommand : AbstractCommand("remindme", listOf("lembre", "remind", "lembrar", "lembrete", "reminder"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "tempo mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["LEMBRAR_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("dar comida para o dog", "lista");
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var message = context.strippedArgs.joinToString(separator = " ")

			if (message == "lista" || message == "list") {
				handleReminderList(context, 0, locale)
				return
			}

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

	fun handleReminderList(context: CommandContext, page: Int, locale: BaseLocale) {
		val reminders = context.lorittaUser.profile.reminders
		val visReminders = reminders.subList(page * 9, Math.min((page * 9) + 9, reminders.size))
		val embed = EmbedBuilder()
		embed.setTitle("<a:lori_notification:394165039227207710> ${locale["LEMBRAR_YourReminders"]} (${reminders.size})")
		embed.setColor(Color(255, 179, 43))

		for ((idx, reminder) in visReminders.withIndex()) {
			embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.reason}\n")
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.name == "➡") {
				message.delete().complete()
				handleReminderList(context, page + 1, locale)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.name == "⬅") {
				message.delete().complete()
				handleReminderList(context, page - 1, locale)
				return@onReactionAddByAuthor
			}

			val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

			if (idx == -1) // derp
				return@onReactionAddByAuthor

			val reminder = visReminders.getOrNull(idx) ?: return@onReactionAddByAuthor

			val guild = if (reminder.guild != null) {
				lorittaShards.getGuildById(reminder.guild!!)
			} else {
				null
			}

			val textChannel = if (guild != null) {
				guild.getTextChannelById(reminder.textChannel)
			} else {
				null
			}

			val embed = EmbedBuilder()
			if (guild != null) {
				embed.setThumbnail(guild.iconUrl)
			}

			embed.setTitle("<a:lori_notification:394165039227207710> ${reminder.reason}")
			embed.appendDescription("**${locale["LEMBRAR_RemindAt"]} ** ${reminder.remindMe.humanize(locale)}\n")
			embed.appendDescription("**${locale["LEMBRAR_CreatedInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
			embed.appendDescription("**${locale["LEMBRAR_RemindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
			embed.setColor(Color(255, 179, 43))

			message.clearReactions().complete()
			message.editMessage(embed.build()).complete()

			message.onReactionAddByAuthor(context) {
				message.delete().complete()
				reminders.remove(reminder)
				loritta.usersColl.updateOne(Filters.eq("_id", context.userHandle.id), Document("\$set", Document("reminders", reminders)))

				context.reply(
						LoriReply(
								locale["LEMBRAR_ReminderRemoved"],
								"\uD83D\uDDD1"
						)
				)
				return@onReactionAddByAuthor
			}

			message.addReaction("\uD83D\uDDD1").complete()
			return@onReactionAddByAuthor
		}

		if (page != 0)
			message.addReaction("⬅").complete()

		for ((idx, _) in visReminders.withIndex()) {
			message.addReaction(Constants.INDEXES[idx]).complete()
		}

		if (((page + 1) * 9) in 0..reminders.size) {
			message.addReaction("➡").complete()
		}
	}
}