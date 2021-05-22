package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import org.jetbrains.exposed.sql.deleteWhere
import java.awt.Color
import java.util.*

class LembrarCommand : AbstractCommand("remindme", listOf("lembre", "remind", "lembrar", "lembrete", "reminder"), CommandCategory.UTILS) {
	override fun getBotPermissions() = listOf(Permission.MESSAGE_MANAGE)

	override fun getDescriptionKey() = LocaleKeyData("${LOCALE_PREFIX}.description")
	override fun getExamplesKey() = LocaleKeyData("${LOCALE_PREFIX}.examples")
	// TODO: Fix Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (thereIsCommandToProcess(context)) {
			val message = getMessage(context)
			if ( message.isAValidListCommand() ) {
				handleReminderList(context, 0, locale)
				return
			}

			val reply = createReply(context, locale)
			createResponseByAuthor(reply, context, message, locale)
			createReactionAddByAuthor(reply, context, locale)
			reply.addReaction("\uD83D\uDE45").queue()
		} else {
			this.explain(context)
		}
	}

	private fun createReactionAddByAuthor(reply: Message, context: CommandContext, locale: BaseLocale) {
		reply.onReactionAddByAuthor(context) {
			loritta.messageInteractionCache.remove(reply.idLong)
			reply.delete().queue()
			context.reply(
					LorittaReply(
							message = locale["$LOCALE_PREFIX.cancel"],
							prefix = "\uD83D\uDDD1"
					)
			)
		}
	}

	private fun createResponseByAuthor(reply: Message, context: CommandContext, message: String, locale: BaseLocale) {
		reply.onResponseByAuthor(context) {
			loritta.messageInteractionCache.remove(reply.idLong)
			reply.delete().queue()
			val inMillis = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = inMillis

			val messageContent = message.trim()
			logger.trace { "userId = ${context.userHandle.idLong}" }
			logger.trace { "channelId = ${context.message.channel.idLong}" }
			logger.trace { "remindAt = $inMillis" }
			logger.trace { "content = $messageContent" }

			createReminder(context, calendar, messageContent)

			val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
			val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
			val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
			val minutes = String.format("%02d", calendar[Calendar.MINUTE])
			context.sendMessage(context.getAsMention(true) + locale["${LOCALE_PREFIX}.success", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes])
		}
	}

	private suspend fun createReminder(context: CommandContext, calendar: Calendar, messageContent: String) {
		loritta.newSuspendedTransaction {
			Reminder.new {
				userId = context.userHandle.idLong
				channelId = context.message.textChannel.idLong
				remindAt = calendar.timeInMillis
				content = messageContent
			}
		}
	}

	private suspend fun createReply(context: CommandContext, locale: BaseLocale): Message {
		return context.reply(
				LorittaReply(
						message = locale["${LOCALE_PREFIX}.setHour"],
						prefix = "⏰"
				)
		)
	}

	private fun getMessage(context: CommandContext) =
			context.strippedArgs.joinToString(separator = " ")

	private fun thereIsCommandToProcess(context: CommandContext) =
			context.args.isNotEmpty()

	private suspend fun handleReminderList(context: CommandContext, page: Int, locale: BaseLocale) {
		val reminders = loritta.newSuspendedTransaction {
			Reminder.find { Reminders.userId eq context.userHandle.idLong }.toMutableList()
		}

		val visReminders = reminders.subList(page * 9, Math.min((page * 9) + 9, reminders.size))
		val embed = EmbedBuilder()
		embed.setTitle("<a:lori_notification:394165039227207710> ${locale["${LOCALE_PREFIX}.yourReminders"]} (${reminders.size})")
		embed.setColor(Color(255, 179, 43))

		for ((idx, reminder) in visReminders.withIndex()) {
			embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.content.substringIfNeeded(0..100)}\n")
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

			val embedBuilder = EmbedBuilder()
			if (guild != null) {
				embedBuilder.setThumbnail(guild.iconUrl)
			}

			embedBuilder.setTitle("<a:lori_notification:394165039227207710> ${reminder.content}".substringIfNeeded(0 until MessageEmbed.TITLE_MAX_LENGTH))
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.remindAt"]} ** ${reminder.remindAt.humanize(locale)}\n")
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.createdInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
			embedBuilder.appendDescription("**${locale["${LOCALE_PREFIX}.remindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
			embedBuilder.setColor(Color(255, 179, 43))

			message.clearReactions().queue()
			message.editMessage(embedBuilder.build()).queue()
			message.addReaction("⬅️").queue()

			message.onReactionAddByAuthor(context) {

				if (it.reactionEmote.isEmote("⬅️")) {

					message.delete().queue()
					handleReminderList(context, page, locale)
					return@onReactionAddByAuthor

				}

				message.delete().queue()
				reminders.remove(reminder)
				loritta.newSuspendedTransaction {
					Reminders.deleteWhere { Reminders.id eq reminder.id }
				}

				val successMessage = context.sendMessage(locale["${LOCALE_PREFIX}.reminderRemoved"])
				successMessage.onReactionAddByAuthor(context) {
					successMessage.delete().queue()
					handleReminderList(context, page, locale)
				}
				successMessage.addReaction("⬅️").queue()
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

	private companion object {
		private const val LOCALE_PREFIX = "commands.command.remindme"
	}
}

private fun String.isAValidListCommand(): Boolean {
	val validListCommands = listOf("lista", "list")
	return 	validListCommands.contains(this)
}
