package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.response.responses.*

class ServerSupportModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return event.channel.id == "398987569485971466" && Loritta.config.environment == EnvironmentType.PRODUCTION
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val content = event.message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")

		val responses = listOf(
				LoriOfflineResponse(),
				LanguageResponse(),
				MentionChannelResponse(),
				MusicResponse(),
				StarboardResponse(),
				LimparPlaylistResponse(),
				AddEmotesResponse(),
				SendFanArtsResponse(),
				LoriMandarComandosResponse()
		)

		responses.forEach {
			if (it.handleResponse(event, content))
				event.channel.sendMessage(it.getResponse(event, content)).complete()
		}

		return false
	}
}