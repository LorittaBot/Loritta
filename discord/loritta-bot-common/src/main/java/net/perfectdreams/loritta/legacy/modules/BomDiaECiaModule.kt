package net.perfectdreams.loritta.legacy.modules

import net.perfectdreams.loritta.legacy.dao.Profile
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.events.LorittaMessageEvent
import net.perfectdreams.loritta.legacy.utils.BomDiaECia
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.dao.servers.moduleconfigs.MiscellaneousConfig

class BomDiaECiaModule : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig)
		return miscellaneousConfig?.enableBomDiaECia ?: false
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo(serverConfig.commandPrefix))
		activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
		activeTextChannelInfo.users.add(event.author)
		loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo

		return false
	}
}