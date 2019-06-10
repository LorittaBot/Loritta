package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.*

class LembrarCommand : AbstractCommand("remindme", listOf("lembre", "remind", "lembrar", "lembrete", "reminder"), CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "tempo mensagem"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["LEMBRAR_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return listOf("dar comida para o dog", "lista")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

			reply.onResponseByAuthor(context) {
				loritta.messageInteractionCache.remove(reply.idLong)
				reply.delete().queue()
				val inMillis = it.message.contentDisplay.convertToEpochMillisRelativeToNow()
				val calendar = Calendar.getInstance()
				calendar.timeInMillis = inMillis

				val message = message.trim()
				logger.trace { "userId = ${context.userHandle.idLong}" }
				logger.trace { "channelId = ${context.message.channel.idLong}" }
				logger.trace { "remindAt = $inMillis" }
				logger.trace { "content = $message" }

				// Criar o Lembrete
				transaction(Databases.loritta) {
					Reminder.new {
						userId = context.userHandle.idLong
						channelId = context.message.textChannel.idLong
						remindAt = calendar.timeInMillis
						content = message
					}
				}

				val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
				val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
				val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
				val minutes = String.format("%02d", calendar[Calendar.MINUTE])
				context.sendMessage(context.getAsMention(true) + locale["LEMBRAR_SUCCESS", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes])
			}

			reply.onReactionAddByAuthor(context) {
				loritta.messageInteractionCache.remove(reply.idLong)
				reply.delete().queue()
				context.reply(
						LoriReply(
								message = locale["LEMBRAR_Cancelado"],
								prefix = "\uD83D\uDDD1"
						)
				)
			}

			reply.addReaction("\uD83D\uDE45").queue()
		} else {
			this.explain(context)
		}
	}

	suspend fun handleReminderList(context: CommandContext, page: Int, locale: LegacyBaseLocale) {
		val reminders = transaction(Databases.loritta) {
			Reminder.find { Reminders.userId eq context.userHandle.idLong }.toMutableList()
		}

		val visReminders = reminders.subList(page * 9, Math.min((page * 9) + 9, reminders.size))
		val embed = EmbedBuilder()
		embed.setTitle("<a:lori_notification:394165039227207710> ${locale["LEMBRAR_YourReminders"]} (${reminders.size})")
		embed.setColor(Color(255, 179, 43))

		for ((idx, reminder) in visReminders.withIndex()) {
			embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.content}\n")
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("➡")) {
				message.delete().queue()
				handleReminderList(context, page + 1, locale)
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("⬅")) {
				message.delete().queue()
				handleReminderList(context, page - 1, locale)
				return@onReactionAddByAuthor
			}

			val idx = Constants.INDEXES.indexOf(it.reactionEmote.name)

			if (idx == -1) // derp
				return@onReactionAddByAuthor

			val reminder = visReminders.getOrNull(idx) ?: return@onReactionAddByAuthor

			val textChannel = lorittaShards.getTextChannelById(reminder.channelId.toString())

			val guild = textChannel?.guild

			val embed = EmbedBuilder()
			if (guild != null) {
				embed.setThumbnail(guild.iconUrl)
			}

			embed.setTitle("<a:lori_notification:394165039227207710> ${reminder.content}")
			embed.appendDescription("**${locale["LEMBRAR_RemindAt"]} ** ${reminder.remindAt.humanize(locale)}\n")
			embed.appendDescription("**${locale["LEMBRAR_CreatedInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
			embed.appendDescription("**${locale["LEMBRAR_RemindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
			embed.setColor(Color(255, 179, 43))

			message.clearReactions().queue()
			message.editMessage(embed.build()).queue()

			message.onReactionAddByAuthor(context) {
				message.delete().queue()
				reminders.remove(reminder)
				transaction(Databases.loritta) {
					Reminders.deleteWhere { Reminders.id eq reminder.id }
				}

				context.reply(
						LoriReply(
								locale["LEMBRAR_ReminderRemoved"],
								"\uD83D\uDDD1"
						)
				)
				return@onReactionAddByAuthor
			}

			message.addReaction("\uD83D\uDDD1").queue()
			return@onReactionAddByAuthor
		}

		if (page != 0)
			message.addReaction("⬅").queue()

		for ((idx, _) in visReminders.withIndex()) {
			message.addReaction(Constants.INDEXES[idx]).queue()
		}

		if (((page + 1) * 9) in 0..reminders.size) {
			message.addReaction("➡").queue()
		}
	}
}