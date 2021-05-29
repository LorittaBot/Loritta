package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KLogger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.tables.ExecutedCommandsLog
import org.jetbrains.exposed.sql.insert

object CommandUtils {
	/**
	 * Logs the [event] to the provided [logger], this is useful to log executed commands to the [logger]
	 *
	 * @param event  the message event
	 * @param logger the logger
	 * @see logMessageEventComplete
	 */
	fun logMessageEvent(event: LorittaMessageEvent, logger: KLogger) {
		if (event.message.isFromType(ChannelType.TEXT)) {
			logger.info("(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}")
		} else {
			logger.info("(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}")
		}
	}

	/**
	 * Logs the [event] to the provided [logger] with a calculated latency, this is useful to log executed commands to the [logger] after they finished its execution
	 *
	 * @param event          the message event
	 * @param logger         the logger
	 * @param commandLatency how much time it took to process the event
	 * @see logMessageEvent
	 */
	fun logMessageEventComplete(event: LorittaMessageEvent, logger: KLogger, commandLatency: Long) {
		if (event.message.isFromType(ChannelType.TEXT)) {
			logger.info("(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processed in ${commandLatency}ms")
		} else {
			logger.info("(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processed in ${commandLatency}ms")
		}
	}

	/**
	 * Tracks a command execution to the database, logging information about the message and the command
	 *
	 * @param event     the message event
	 * @param clazzName the class name of the command
	 */
	suspend fun trackCommandToDatabase(event: LorittaMessageEvent, clazzName: String) {
		loritta.newSuspendedTransaction {
			ExecutedCommandsLog.insert {
				it[userId] = event.author.idLong
				it[ExecutedCommandsLog.guildId] = if (event.message.isFromGuild) event.message.guild.idLong else null
				it[channelId] = event.message.channel.idLong
				it[sentAt] = System.currentTimeMillis()
				it[ExecutedCommandsLog.command] = clazzName
				it[ExecutedCommandsLog.message] = event.message.contentRaw
			}
		}
	}

	suspend fun checkIfCommandIsDisabledInGuild(serverConfig: ServerConfig, locale: BaseLocale, channel: MessageChannel, member: Member, clazzName: String): Boolean {
		if (serverConfig.disabledCommands.contains(clazzName)) {
			val replies = mutableListOf(
				LorittaReply(
					locale["commands.commandDisabled"],
					Emotes.LORI_CRYING
				)
			)

			if (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)) {
				replies.add(
					LorittaReply(
						locale["commands.howToReEnableCommands", "<${loritta.instanceConfig.loritta.website.url}guild/${member.guild.idLong}/configure/commands>"],
						Emotes.LORI_SMILE
					)
				)
			}

			channel.sendMessageAsync(
				replies.joinToString("\n") {
					it.build(JDAUser(member.user))
				}
			)
			return true
		}

		return false
	}
}