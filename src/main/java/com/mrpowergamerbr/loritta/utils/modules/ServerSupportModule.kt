package com.mrpowergamerbr.loritta.utils.modules

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.response.responses.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.regex.Pattern

object ServerSupportModule {
	fun checkForSupport(event: MessageReceivedEvent, message: Message) {
		if (message.textChannel.id != "398987569485971466")
			return

		val content = message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")

		val responses = listOf(
				LoriOfflineResponse(),
				LanguageResponse(),
				MentionChannelResponse(),
				MusicResponse(),
				StarboardResponse(),
				LimparPlaylistResponse()
		)

		responses.forEach {
			if (it.handleResponse(event, content))
				event.channel.sendMessage(it.getResponse(event, content)).complete()
		}
		return
	}
}