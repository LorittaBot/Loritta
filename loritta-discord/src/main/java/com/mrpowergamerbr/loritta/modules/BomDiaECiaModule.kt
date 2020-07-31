package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.BomDiaECia
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.dao.servers.moduleconfigs.MiscellaneousConfig

class BomDiaECiaModule : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig)
		return miscellaneousConfig?.enableBomDiaECia ?: false
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo(serverConfig.commandPrefix))
		activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
		activeTextChannelInfo.users.add(event.author)
		loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo

		return false
	}
}