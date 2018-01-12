package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

class ChatLogCommand : AbstractCommand("chatlog", listOf("backupchat", "chatbackup"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["CHATLOG_Description"]
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_HISTORY)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val history = context.event.channel.history

		var lastCheck = -1
		for (i in 0 until 100) {
			history.retrievePast(100).complete()
			if (lastCheck == history.retrievedHistory.size)
				break
			lastCheck = history.retrievedHistory.size
		}

		val lines = mutableListOf<String>()

		for (message in history.retrievedHistory.reversed()) {
			val dayOfMonth = String.format("%02d", message.creationTime.dayOfMonth)
			val month = String.format("%02d", message.creationTime.monthValue)
			val year = message.creationTime.year

			val hour = String.format("%02d", message.creationTime.hour)
			val minute = String.format("%02d", message.creationTime.minute)

			var line = "[$dayOfMonth/$month/$year $hour:$minute] (${message.author.id}) ${message.author.name}#${message.author.discriminator}: ${message.contentRaw}"
			lines.add(line)
		}

		val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())
		context.sendFile(targetStream, "${context.guild.name}-${System.currentTimeMillis()}.log", context.getAsMention(true))
		targetStream.close()
	}
}