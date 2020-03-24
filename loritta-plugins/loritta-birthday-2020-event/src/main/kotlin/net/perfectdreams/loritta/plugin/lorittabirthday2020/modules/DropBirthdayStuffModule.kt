package net.perfectdreams.loritta.plugin.lorittabirthday2020.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.chance
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.plugin.lorittabirthday2020.LorittaBirthday2020
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Drops
import net.perfectdreams.loritta.plugin.lorittabirthday2020.tables.Birthday2020Players
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
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

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (lorittaProfile == null)
			return false

		return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true && LorittaBirthday2020.isEventActive()
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (lorittaProfile == null)
			return false

		val date = System.currentTimeMillis()

		val millis = event.member!!.timeJoined.toInstant().toEpochMilli()

		val diff = date - millis

		var chance = Math.min((diff.toDouble() * 2.0) / 1_296_000_000, 2.0)

		val id = event.channel.idLong
		val lastDrop = lastDropsAt.getOrDefault(id, 0L)

		val lastDropDiff = System.currentTimeMillis() - lastDrop

		val since = 360_000 - Math.max(360_000 - lastDropDiff, 0)

		val chanceBoost = (8.0 * since) / 360_000

		val ceil = 22.0

		chance = Math.min(chance + chanceBoost, ceil)

		chance = 100.0

		if (chance(chance) && event.message.contentStripped.hashCode() != lorittaProfile?.lastMessageSentHash) {
			/* if (5_000 >= System.currentTimeMillis() - lastDrop)
				return false

			val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

			if (180_000 >= System.currentTimeMillis() - userDropTime)
				return false */

			/* val isParticipating = transaction(Databases.loritta) {
				Christmas2019Players.select {
					Christmas2019Players.user eq lorittaProfile.id
				}.count() != 0
			}

			val collectedAll = transaction(Databases.loritta) {
				CollectedChristmas2019Points.selectAll().count()
			} */
			val collectedAll = 0

			val getTheCandy = transaction(Databases.loritta) {
				Birthday2020Players.select {
					Birthday2020Players.user eq lorittaProfile.id
				}.count() != 0
			}

			val emoteToBeUsed = try {
				val day = Calendar.getInstance()[Calendar.DAY_OF_MONTH]

				val magicChance = 0.0 /* if (day == 29) {
					5.0
				} else if (day == 30) {
					7.5
				} else {
					20.0
				} */

				if (chance(magicChance)) {
					"\uD83C\uDF20"
				} else {
					LorittaBirthday2020.emojis[(collectedAll / 50_000) % LorittaBirthday2020.emojis.size]
				}
			} catch (e: Exception) {
				logger.warn(e) { "Invalid Christmas emote! ${(collectedAll / 50_000) % LorittaBirthday2020.emojis.size}" }
				return false
			}

			if (getTheCandy) {
				lastDropsAt[id] = System.currentTimeMillis()
				lastDropsByUserAt[event.author.idLong] = System.currentTimeMillis()
				event.message.addReaction(emoteToBeUsed).queue {
					dropInMessageAt[event.message.idLong] = System.currentTimeMillis()
				}
				transaction(Databases.loritta) {
					Birthday2020Drops.insert {
						it[guildId] = event.guild!!.idLong
						it[channelId] = event.channel.idLong
						it[messageId] = event.message.idLong
						it[createdAt] = System.currentTimeMillis()
					}
				}
			}
		}

		return false
	}
}