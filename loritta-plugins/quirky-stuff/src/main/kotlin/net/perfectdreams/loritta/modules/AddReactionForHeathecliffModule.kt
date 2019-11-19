package net.perfectdreams.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class AddReactionForHeathecliffModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		return event.channel.idLong == 643828343325851648L
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (!event.message.contentRaw.startsWith(">")) {
			event.message.addReaction("\uD83D\uDC4D").queue()
			event.message.addReaction("stonks:643608960720699464").queue()
			event.message.addReaction("baka:473905338220019732").queue()
			event.message.addReaction("❤").queue()
		}

		return false
	}
}