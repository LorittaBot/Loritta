package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.BomDiaECia
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import org.jetbrains.exposed.sql.transactions.transaction

class BomDiaECiaModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val miscellaneousConfig = transaction(Databases.loritta) {
			serverConfig.miscellaneousConfig
		}

		val enableBomDiaECia = miscellaneousConfig?.enableBomDiaECia ?: false

		return enableBomDiaECia
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo(serverConfig.commandPrefix))
		activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
		activeTextChannelInfo.users.add(event.author)
		loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo

		return false
	}
}