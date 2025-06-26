package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.extensions.sendMessageAsync
import mu.KLogger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.perfectdreams.harmony.logging.HarmonyLogger
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.sql.Connection

object CommandUtils {
	/**
	 * Logs the [event] to the provided [logger], this is useful to log executed commands to the [logger]
	 *
	 * @param event  the message event
	 * @param logger the logger
	 * @see logMessageEventComplete
	 */
	fun logMessageEvent(event: LorittaMessageEvent, logger: HarmonyLogger) {
		if (event.message.isFromType(ChannelType.TEXT)) {
			logger.info { "(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}" }
		} else {
			logger.info { "(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}" }
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
	fun logMessageEventComplete(event: LorittaMessageEvent, logger: HarmonyLogger, commandLatency: Long) {
		if (event.message.isFromType(ChannelType.TEXT)) {
			logger.info { "(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processed in ${commandLatency}ms" }
		} else {
			logger.info { "(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processed in ${commandLatency}ms" }
		}
	}

	/**
	 * Tracks a command execution to the database, logging information about the message and the command
	 *
	 * @param event     the message event
	 * @param clazzName the class name of the command
	 */
	suspend fun trackCommandToDatabase(loritta: LorittaBot, event: LorittaMessageEvent, clazzName: String) {
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

	suspend fun checkIfCommandIsDisabledInGuild(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, channel: MessageChannel, member: Member, clazzName: String): Boolean {
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
						locale["commands.howToReEnableCommands", "<${loritta.config.loritta.website.url}guild/${member.guild.idLong}/configure/commands>"],
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