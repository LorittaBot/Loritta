package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.BomDiaECia
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class BomDiaECiaModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return serverConfig.miscellaneousConfig.enableBomDiaECia
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo())
		activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
		activeTextChannelInfo.users.add(event.author)
		loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo

		return false
	}
}