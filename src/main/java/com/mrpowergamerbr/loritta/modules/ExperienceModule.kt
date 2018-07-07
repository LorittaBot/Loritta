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
			val calculatedMessageSpeed = event.message.contentStripped.toLowerCase().length.toDouble() / 7

			val diff = System.currentTimeMillis() - lorittaProfile.lastMessageSent

			if (diff > calculatedMessageSpeed * 1000) {
				var nonRepeatedCharsMessage = event.message.contentStripped.replace(Regex("(.)\\1{1,}"), "$1")

				if (nonRepeatedCharsMessage.length >= 12) {
					val gainedXp = Math.min(35, Loritta.RANDOM.nextInt(Math.max(1, nonRepeatedCharsMessage.length / 7), (Math.max(2, nonRepeatedCharsMessage.length / 4))))

					var globalGainedXp = gainedXp

					val isDonator = lorittaProfile.isDonator && System.currentTimeMillis() > lorittaProfile.donationExpiresIn
					if (isDonator) {
						globalGainedXp = when {
							lorittaProfile.donatorPaid >= 89.99 -> (globalGainedXp * 1.75).toInt()
							lorittaProfile.donatorPaid >= 69.99 -> (globalGainedXp * 1.75).toInt()
							lorittaProfile.donatorPaid >= 49.99 -> (globalGainedXp * 1.5).toInt()
							lorittaProfile.donatorPaid >= 29.99 -> (globalGainedXp * 1.25).toInt()
							else -> globalGainedXp
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