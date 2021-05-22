package net.perfectdreams.loritta.plugin.lorittabirthday2020.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.listeners.GetBirthdayStuffListener
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Drops
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class DropBirthdayStuffModule : MessageReceivedModule {
	val lastDropsAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
			.asMap()
	val lastDropsByUserAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
			.asMap()
	companion object {
		val dropInMessageAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<Long, Long>()
				.asMap()
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (lorittaProfile == null)
			return false

		return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true && LorittaBirthday2020.isEventActive()
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (lorittaProfile == null)
			return false

		val date = System.currentTimeMillis()

		val millis = event.member!!.timeJoined.toInstant().toEpochMilli()

		val diff = date - millis

		var chance = (Math.min((diff.toDouble() * 100.0) / 1_296_000_000, 100.0) - 1).toInt()

		val endOfBotsRestriction = LocalDateTime.of(2020, 3, 29, 15, 0)
				.atZone(ZoneId.of("America/Sao_Paulo"))
		val now = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))

		if (endOfBotsRestriction.isBefore(now)) {
			val infractions = LorittaBirthday2020.detectedBotGuilds[event.guild!!.idLong]

			if (infractions != null) {
				val mutex = GetBirthdayStuffListener.mutexes.getOrPut(event.guild!!.idLong, { Mutex() })
				mutex.withLock {
					// Iremos apenas pegar infrações enviadas a menos de 30 minutos
					val activeInfractions = infractions.filter { it.detectedAt >= System.currentTimeMillis() - 1_800_000 }
					infractions.clear()
					infractions.addAll(activeInfractions)
					chance -= (activeInfractions.size * 6)
					if (activeInfractions.isEmpty())
						LorittaBirthday2020.detectedBotGuilds.remove(event.guild!!.idLong)
				}
			}
		}

		if (event.guild!!.idLong in LorittaBirthday2020.blacklistedGuilds)
			return false

		if (0 >= chance)
			return false

		val id = event.channel.idLong

		val lastDrop = lastDropsAt.getOrDefault(id, 0L)
		val lastDropDiff = date - lastDrop

		val randomNumber = Loritta.RANDOM.nextInt(0, 750) // Loritta.RANDOM.nextInt(0, 1500)

		if (randomNumber in 0..chance && event.message.contentStripped.hashCode() != lorittaProfile.lastMessageSentHash && event.message.contentRaw.length >= 5) {
			if (5_000 >= lastDropDiff)
				return false

			val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

			if (30_000 >= date - userDropTime)
				return false

			val getTheCandy = transaction(Databases.loritta) {
				Birthday2020Players.select {
					Birthday2020Players.user eq lorittaProfile.id
				}.count() != 0L
			}

			val emoteToBeUsed = LorittaBirthday2020.emojis.random()

			if (getTheCandy) {
				lastDropsAt[id] = date
				lastDropsByUserAt[event.author.idLong] = date
				event.message.addReaction(emoteToBeUsed).queue {
					dropInMessageAt[event.message.idLong] = date
				}
				transaction(Databases.loritta) {
					Birthday2020Drops.insert {
						it[guildId] = event.guild!!.idLong
						it[channelId] = event.channel.idLong
						it[messageId] = event.message.idLong
						it[createdAt] = date
					}
				}
			}
		}

		return false
	}
}