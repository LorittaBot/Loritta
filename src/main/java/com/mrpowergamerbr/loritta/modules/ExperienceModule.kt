package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save

class ExperienceModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		// (copyright Loritta™)

		// Primeiro iremos ver se a mensagem contém algo "interessante"
		if (event.message.contentStripped.length >= 5 && lorittaProfile.lastMessageSentHash != event.message.contentStripped.hashCode()) {
			// Primeiro iremos verificar se a mensagem é "válida"
			// 7 chars por millisegundo
			var calculatedMessageSpeed = event.message.contentStripped.toLowerCase().length.toDouble() / 7

			var diff = System.currentTimeMillis() - lorittaProfile.lastMessageSent

			if (diff > calculatedMessageSpeed * 1000) {
				var nonRepeatedCharsMessage = event.message.contentStripped.replace(Regex("(.)\\1{1,}"), "$1")

				if (nonRepeatedCharsMessage.length >= 12) {
					var gainedXp = Math.min(35, Loritta.RANDOM.nextInt(Math.max(1, nonRepeatedCharsMessage.length / 7), (Math.max(2, nonRepeatedCharsMessage.length / 4))))

					var globalGainedXp = gainedXp

					val lorittaGuild = com.mrpowergamerbr.loritta.utils.lorittaShards.getGuildById("297732013006389252")

					if (lorittaGuild != null) {
						val xpBoost1 = lorittaGuild.getRoleById("436919257993969666") // Pagadores de Aluguel
						val xpBoost2 = lorittaGuild.getRoleById("435856512787677214") // Contribuidor Inativo

						if (event.member!!.roles.contains(xpBoost1)) {
							var _gainedXp = gainedXp
							_gainedXp = (_gainedXp * 1.25).toInt()
							globalGainedXp = _gainedXp
						}

						if (event.member!!.roles.contains(xpBoost2)) {
							var _gainedXp = gainedXp
							_gainedXp = (_gainedXp * 1.5).toInt()
							globalGainedXp = _gainedXp
						}
					}

					lorittaProfile.xp = lorittaProfile.xp + globalGainedXp
					lorittaProfile.lastMessageSentHash = event.message.contentStripped.hashCode()

					val userData = serverConfig.getUserData(event.member!!.user.id)
					userData.xp = userData.xp + gainedXp
					loritta save serverConfig
				}
			}
		}

		lorittaProfile.lastMessageSent = System.currentTimeMillis()

		return false
	}
}