package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class ExperienceModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		// (copyright Loritta™)
		var newProfileXp = lorittaProfile.xp
		var lastMessageSentHash: Int? = null

		// Primeiro iremos ver se a mensagem contém algo "interessante"
		if (event.message.contentStripped.length >= 5 && lorittaProfile.lastMessageSentHash != event.message.contentStripped.hashCode()) {
			// Primeiro iremos verificar se a mensagem é "válida"
			// 7 chars por millisegundo
			val calculatedMessageSpeed = event.message.contentStripped.toLowerCase().length.toDouble() / 7

			val diff = System.currentTimeMillis() - lorittaProfile.lastMessageSentAt

			if (diff > calculatedMessageSpeed * 1000) {
				val nonRepeatedCharsMessage = event.message.contentStripped.replace(Constants.REPEATING_CHARACTERS_REGEX, "$1")

				if (nonRepeatedCharsMessage.length >= 12) {
					val gainedXp = Math.min(35, Loritta.RANDOM.nextInt(Math.max(1, nonRepeatedCharsMessage.length / 7), (Math.max(2, nonRepeatedCharsMessage.length / 4))))

					var globalGainedXp = gainedXp

					if (lorittaProfile.isActiveDonator()) {
						globalGainedXp = when {
							lorittaProfile.donatorPaid >= 159.99 -> (globalGainedXp * 2.5).toInt()
							lorittaProfile.donatorPaid >= 139.99 -> (globalGainedXp * 2.25).toInt()
							lorittaProfile.donatorPaid >= 119.99 -> (globalGainedXp * 2.0).toInt()
							lorittaProfile.donatorPaid >= 99.99 -> (globalGainedXp * 1.75).toInt()
							lorittaProfile.donatorPaid >= 79.99 -> (globalGainedXp * 1.5).toInt()
							lorittaProfile.donatorPaid >= 59.99 -> (globalGainedXp * 1.25).toInt()
							lorittaProfile.donatorPaid >= 39.99 -> (globalGainedXp * 1.1).toInt()
							else -> globalGainedXp
						}
					}

					newProfileXp = lorittaProfile.xp + globalGainedXp
					lastMessageSentHash = event.message.contentStripped.hashCode()

					val profile = serverConfig.getUserData(event.author.idLong)

					transaction(Databases.loritta) {
						profile.xp += gainedXp
					}
				}
			}
		}

		if (lastMessageSentHash != null && lorittaProfile.xp != newProfileXp) {
			transaction(Databases.loritta) {
				lorittaProfile.lastMessageSentHash = lastMessageSentHash
				lorittaProfile.xp = newProfileXp
				lorittaProfile.lastMessageSentAt = System.currentTimeMillis()
			}
		}
		return false
	}
}