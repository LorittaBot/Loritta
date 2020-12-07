package net.perfectdreams.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.dao.Reminder
import com.mrpowergamerbr.loritta.tables.Reminders
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import org.jetbrains.exposed.sql.deleteWhere
import java.awt.Color
import java.util.*

class LembrarCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("remindme", "lembre", "remind", "lembrar", "lembrete", "reminder"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.remindme"
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		botRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

		usage {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.TEXT) {}
		}

		localizedDescription("$LOCALE_PREFIX.description")

		localizedExamples("$LOCALE_PREFIX.examples")

		executesDiscord {
			val context = this

			if (thereIsCommandToProcess(context)) {
				val message = getMessage(context)
				if ( message.isAValidListCommand() ) {
					handleReminderList(context, 0, locale)
					return@executesDiscord
				}

				val reply = createReply(context, locale)
				createResponseByAuthor(reply, context, message, locale)
				createReactionAddByAuthor(reply, context, locale)
				reply.addReaction("\uD83D\uDE45").queue()
			} else {
				explain()
			}
		}
	}

	private fun createReactionAddByAuthor(reply: Message, context: DiscordCommandContext, locale: BaseLocale) {
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

	private fun createResponseByAuthor(reply: Message, context: DiscordCommandContext, message: String, locale: BaseLocale) {
		reply.onResponseByAuthor(context) {
			loritta.messageInteractionCache.remove(reply.idLong)
			reply.delete().queue()
			val inMillis = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)
			val calendar = Calendar.getInstance()
			calendar.timeInMillis = inMillis

			val messageContent = message.trim()
			logger.trace { "userId = ${context.user.idLong}" }
			logger.trace { "channelId = ${context.discordMessage.textChannel.idLong}" }
			logger.trace { "remindAt = $inMillis" }
			logger.trace { "content = $messageContent" }

			createReminder(context, calendar, messageContent)

			val dayOfMonth = String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
			val month = String.format("%02d", calendar[Calendar.MONTH] + 1)
			val hours = String.format("%02d", calendar[Calendar.HOUR_OF_DAY])
			val minutes = String.format("%02d", calendar[Calendar.MINUTE])
			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.success", dayOfMonth, month, calendar[Calendar.YEAR], hours, minutes],
							"\uD83C\uDF89"
					)
			)
		}
	}

	private suspend fun createReminder(context: DiscordCommandContext, calendar: Calendar, messageContent: String) {
		loritta.newSuspendedTransaction {
			Reminder.new {
				userId = context.user.idLong
				channelId = context.discordMessage.textChannel.idLong
				remindAt = calendar.timeInMillis
				content = messageContent
			}
		}
	}

	private suspend fun createReply(context: DiscordCommandContext, locale: BaseLocale): Message {
		return context.discordMessage.channel.sendMessage("⏰ **|** ${locale["$LOCALE_PREFIX.setHour"]}").await()
	}

	private fun getMessage(context: DiscordCommandContext) =
			context.args.joinToString(separator = " ")

	private fun thereIsCommandToProcess(context: DiscordCommandContext) =
			context.args.isNotEmpty()

	private suspend fun handleReminderList(context: DiscordCommandContext, page: Int, locale: BaseLocale) {
		val reminders = loritta.newSuspendedTransaction {
			Reminder.find { Reminders.userId eq context.user.idLong }.toMutableList()
		}

		val visReminders = reminders.subList(page * 9, Math.min((page * 9) + 9, reminders.size))
		val embed = EmbedBuilder()
		embed.setTitle("<a:lori_notification:394165039227207710> ${locale["$LOCALE_PREFIX.yourReminders"]} (${reminders.size})")
		embed.setColor(Color(255, 179, 43))

		for ((idx, reminder) in visReminders.withIndex()) {
			embed.appendDescription(Constants.INDEXES[idx] + " ${reminder.content.substringIfNeeded(0..100)}\n")
		}

		val message = context.sendMessage(context.getUserMention(true), embed.build())

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
			embedBuilder.appendDescription("**${locale["$LOCALE_PREFIX.remindAt"]} ** ${reminder.remindAt.humanize(locale)}\n")
			embedBuilder.appendDescription("**${locale["$LOCALE_PREFIX.createdInGuild"]}** `${guild?.name ?: "Servidor não existe mais..."}`\n")
			embedBuilder.appendDescription("**${locale["$LOCALE_PREFIX.remindInTextChannel"]}** ${textChannel?.asMention ?: "Canal de texto não existe mais..."}")
			embedBuilder.setColor(Color(255, 179, 43))

			message.clearReactions().queue()
			message.editMessage(embedBuilder.build()).queue()

			message.onReactionAddByAuthor(context) {
				message.delete().queue()
				reminders.remove(reminder)
				loritta.newSuspendedTransaction {
					Reminders.deleteWhere { Reminders.id eq reminder.id }
				}

				context.reply(
						LorittaReply(
								locale["$LOCALE_PREFIX.reminderRemoved"],
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

	private fun String.isAValidListCommand(): Boolean {
		val validListCommands = listOf("lista", "list")
		return 	validListCommands.contains(this)
	}
}