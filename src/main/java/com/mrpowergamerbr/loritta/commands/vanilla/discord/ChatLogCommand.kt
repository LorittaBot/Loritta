package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.DateUtils
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

class ChatLogCommand : AbstractCommand("chatlog", listOf("backupchat", "chatbackup"), CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CHATLOG_Description"]
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_HISTORY)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val history = context.event.channel.history

		var lastCheck = -1
		for (i in 0 until 100) {
			history.retrievePast(100).await()
			if (lastCheck == history.retrievedHistory.size)
				break
			lastCheck = history.retrievedHistory.size
		}

		val lines = mutableListOf<String>()

		for (message in history.retrievedHistory.reversed()) {
			val creationTime = message.timeCreated

			val line = "[${creationTime.format(DateUtils.PRETTY_DATE_FORMAT)}] (${message.id}) ${message.author.name}#${message.author.discriminator}: ${message.contentRaw}"
			lines.add(line)
		}

		val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())
		context.sendFile(targetStream, "${context.guild.name}-${System.currentTimeMillis()}.log", context.getAsMention(true))
		targetStream.close()
	}
}