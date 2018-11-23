package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.bson.conversions.Bson
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ExperienceModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		// (copyright Loritta™)
		val profileUpdates = mutableListOf<Bson>()
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

					val isDonator = lorittaProfile.isDonator && lorittaProfile.donationExpiresIn > System.currentTimeMillis()
					if (isDonator) {
						globalGainedXp = when {
							lorittaProfile.donatorPaid >= 89.99 -> (globalGainedXp * 1.75).toInt()
							lorittaProfile.donatorPaid >= 69.99 -> (globalGainedXp * 1.75).toInt()
							lorittaProfile.donatorPaid >= 49.99 -> (globalGainedXp * 1.5).toInt()
							lorittaProfile.donatorPaid >= 29.99 -> (globalGainedXp * 1.25).toInt()
							else -> globalGainedXp
						}
					}

					newProfileXp = lorittaProfile.xp + globalGainedXp
					lastMessageSentHash = event.message.contentStripped.hashCode()

					transaction(Databases.loritta) {
						GuildProfiles.update({ (GuildProfiles.guildId eq event.guild!!.idLong) and (GuildProfiles.userId eq event.author.idLong) }) {
							with(SqlExpressionBuilder) {
								it.update(GuildProfiles.xp, GuildProfiles.xp + gainedXp.toLong())
							}
						}
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