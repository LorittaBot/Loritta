package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class ExperienceModule : MessageReceivedModule {
	// Para evitar "could not serialize access due to concurrent update", vamos sincronizar o update de XP usando mutexes
	// Como um usuário normalmente só está falando em um servidor ao mesmo tempo, a gente pode sincronizar baseado no User ID dele
	// User ID -> Mutex
	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (!FeatureFlags.isEnabled("experience-gain"))
			return false

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

					val donatorPaid = loritta.getActiveMoneyFromDonations(event.author.idLong)
					if (donatorPaid != 0.0) {
						globalGainedXp = when {
							donatorPaid >= 159.99 -> (globalGainedXp * 2.5).toInt()
							donatorPaid >= 139.99 -> (globalGainedXp * 2.25).toInt()
							donatorPaid >= 119.99 -> (globalGainedXp * 2.0).toInt()
							donatorPaid >= 99.99 -> (globalGainedXp * 1.75).toInt()
							donatorPaid >= 79.99 -> (globalGainedXp * 1.5).toInt()
							donatorPaid >= 59.99 -> (globalGainedXp * 1.25).toInt()
							donatorPaid >= 39.99 -> (globalGainedXp * 1.1).toInt()
							else -> globalGainedXp
						}
					}

					newProfileXp = lorittaProfile.xp + globalGainedXp
					lastMessageSentHash = event.message.contentStripped.hashCode()

					val profile = serverConfig.getUserData(event.author.idLong)

					val mutex = mutexes.getOrPut(event.author.idLong) { Mutex() }

					if (FeatureFlags.isEnabled("experience-gain-locally")) {
						mutex.withLock {
							transaction(Databases.loritta) {
								profile.xp += gainedXp
							}
						}
					}
				}
			}
		}

		if (lastMessageSentHash != null && lorittaProfile.xp != newProfileXp) {
			val mutex = mutexes.getOrPut(event.author.idLong) { Mutex() }

			if (FeatureFlags.isEnabled("experience-gain-globally")) {
				mutex.withLock {
					transaction(Databases.loritta) {
						lorittaProfile.lastMessageSentHash = lastMessageSentHash
						lorittaProfile.xp = newProfileXp
						lorittaProfile.lastMessageSentAt = System.currentTimeMillis()
					}
				}
			}
		}
		return false
	}
}