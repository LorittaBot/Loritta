package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.patreon
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object ExperienceModule {
	fun handleExperience(event: MessageReceivedEvent, serverConfig: ServerConfig, lorittaProfile: LorittaProfile) {
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

					if (event.author.patreon) {
						var _gainedXp = gainedXp
						_gainedXp = (_gainedXp * 1.25).toInt()
						gainedXp = _gainedXp
					}

					lorittaProfile.xp = lorittaProfile.xp + gainedXp
					lorittaProfile.lastMessageSentHash = event.message.contentStripped.hashCode()

					val userData = serverConfig.getUserData(event.member.user.id)
					userData.xp = userData.xp + gainedXp
					loritta save serverConfig
				}
			}
		}

		lorittaProfile.lastMessageSent = System.currentTimeMillis()
	}
}