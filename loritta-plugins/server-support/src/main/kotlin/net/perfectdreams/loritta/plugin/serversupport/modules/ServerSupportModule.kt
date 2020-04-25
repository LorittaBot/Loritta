package net.perfectdreams.loritta.plugin.serversupport.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.perfectdreams.loritta.plugin.serversupport.ServerSupportPlugin

class ServerSupportModule(val plugin: ServerSupportPlugin) : MessageReceivedModule {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		return (event.channel.idLong == 398987569485971466L || event.channel.idLong == 393332226881880074L || event.channel.idLong == 547119872568459284L) && loritta.config.loritta.environment == EnvironmentType.CANARY
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val content = event.message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")
				.toLowerCase()

		for (response in plugin.responses) {
			if (response.handleResponse(event, content)) {
				event.channel.sendMessage(response.getResponse(event, content)!!).queue()
				return false
			}
		}

		return false
	}
}