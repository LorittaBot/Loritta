package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.response.responses.*

class ServerSupportModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return event.channel.id == "398987569485971466" && Loritta.config.environment == EnvironmentType.CANARY
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val content = event.message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")
				.toLowerCase()

		val responses = listOf(
				LanguageResponse(),
				MentionChannelResponse(),
				MusicResponse(),
				StarboardResponse(),
				LimparPlaylistResponse(),
				AddEmotesResponse(),
				SendFanArtsResponse(),
				LoriMandarComandosResponse(),
				HelpMeResponse(),
				LoriOfflineResponse()
		)

		responses.forEach {
			if (it.handleResponse(event, content))
				event.channel.sendMessage(it.getResponse(event, content)).queue()
		}

		return false
	}
}