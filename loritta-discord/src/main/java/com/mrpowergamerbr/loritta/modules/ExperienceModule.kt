package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.perfectdreams.loritta.tables.ExperienceRoleRates
import net.perfectdreams.loritta.tables.LevelAnnouncementConfigs
import net.perfectdreams.loritta.tables.RolesByExperience
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class ExperienceModule : MessageReceivedModule {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	// Para evitar "could not serialize access due to concurrent update", vamos sincronizar o update de XP usando mutexes
	// Como um usuário normalmente só está falando em um servidor ao mesmo tempo, a gente pode sincronizar baseado no User ID dele
	// User ID -> Mutex
	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
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

					val profile = legacyServerConfig.getUserData(event.author.idLong)

					if (FeatureFlags.isEnabled("experience-gain-locally")) {
						handleLocalExperience(event, lorittaProfile, profile, gainedXp, locale.toNewLocale())
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

	suspend fun handleLocalExperience(event: LorittaMessageEvent, profile: Profile, guildProfile: GuildProfile, gainedXp: Int, locale: BaseLocale) {
		val mutex = mutexes.getOrPut(event.author.idLong) { Mutex() }

		val guild = event.guild!!
		val member = event.member!!

		val levelConfig = transaction(Databases.loritta) {
			loritta.getOrCreateServerConfig(guild.idLong).levelConfig
		}

		if (levelConfig != null) {
			logger.info { "Level Config isn't null in $guild" }

			val noXpRoles = levelConfig.noXpRoles

			if (member.roles.any { it.idLong in noXpRoles })
				return

			val noXpChannels = levelConfig.noXpChannels

			if (event.channel.idLong in noXpChannels)
				return
		}

		val (previousLevel, previousXp) = guildProfile.getCurrentLevel()

		mutex.withLock {
			transaction(Databases.loritta) {
				val customRoleRates = ExperienceRoleRates.select {
					ExperienceRoleRates.guildId eq event.guild.idLong and
							(ExperienceRoleRates.role inList member.roles.map { it.idLong })
				}.orderBy(ExperienceRoleRates.rate, SortOrder.DESC)
						.firstOrNull()

				val rate = customRoleRates?.getOrNull(ExperienceRoleRates.rate) ?: 1.0

				guildProfile.xp += (gainedXp * rate).toLong()
			}
		}

		val (newLevel, newXp) = guildProfile.getCurrentLevel()

		var receivedNewRoles = false
		val givenNewRoles = mutableSetOf<Role>()

		if (guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) {
			val configs = transaction(Databases.loritta) {
				RolesByExperience.select {
					RolesByExperience.guildId eq guild.idLong
				}.toMutableList()
			}

			val matched = configs.filter { guildProfile.xp >= it[RolesByExperience.requiredExperience] }
					.sortedByDescending { it[RolesByExperience.requiredExperience] }

			if (matched.isNotEmpty()) {
				val guildRoles = matched.flatMap { it[RolesByExperience.roles].mapNotNull { guild.getRoleById(it) } }
						.filter { guild.selfMember.canInteract(it) } // caso seja um cargo que a Lori não consiga dar, apenas ignore!

				if (guildRoles.isEmpty())
					return

				if (levelConfig?.roleGiveType == RoleGiveType.REMOVE) {
					val topRole = guildRoles.firstOrNull()

					if (topRole != null) {
						val memberNewRoleList = member.roles.toMutableList()

						memberNewRoleList.removeAll(guildRoles)
						memberNewRoleList.add(topRole)

						if (!memberNewRoleList.containsAll(member.roles) || !member.roles.containsAll(memberNewRoleList)) {
							receivedNewRoles = true
							givenNewRoles.add(topRole)
							guild.modifyMemberRoles(member, memberNewRoleList)
									.queue()
						}
					}
				} else {
					val shouldGiveRoles = !member.roles.containsAll(guildRoles)

					if (shouldGiveRoles) {
						val missingRoles = guildRoles.toMutableList().apply { this.removeAll(member.roles) }
						receivedNewRoles = true
						givenNewRoles.addAll(missingRoles)
						guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(missingRoles) })
								.queue()
					}
				}
			}
		}

		if (previousLevel != newLevel && levelConfig != null) {
			logger.info { "Notfying about level up from $previousLevel -> $newLevel; level config is $levelConfig"}

			val announcements = transaction(Databases.loritta) {
				LevelAnnouncementConfigs.select {
					LevelAnnouncementConfigs.levelConfig eq levelConfig.id
				}.toMutableList()
			}

			logger.info { "There are ${announcements.size} announcement stuff!"}

			for (announcement in announcements) {
				val type = announcement[LevelAnnouncementConfigs.type]
				logger.info { "Type is $type" }

				if (announcement[LevelAnnouncementConfigs.onlyIfUserReceivedRoles] && !receivedNewRoles)
					continue

				val message = MessageUtils.generateMessage(
						announcement[LevelAnnouncementConfigs.message],
						listOf(
								member,
								guild
						),
						guild,
						mapOf(
								"previous-level" to previousLevel.toString(),
								"previous-xp" to previousXp.toString(),
								"level" to newLevel.toString(),
								"xp" to newXp.toString(),
								"new-roles" to givenNewRoles.joinToString(transform = { it.asMention })
						)
				)

				logger.info { "Message for notif is $message" }

				if (message != null) {
					when (type) {
						LevelUpAnnouncementType.SAME_CHANNEL -> {
							logger.info { "Same channel, sending msg" }
							if (event.textChannel!!.canTalk()) {
								event.textChannel.sendMessage(
										message
								).queue()
							}
						}
						LevelUpAnnouncementType.DIRECT_MESSAGE -> {
							val profileSettings = transaction(Databases.loritta) {
								profile.settings
							}

							if (!profileSettings.doNotSendXpNotificationsInDm) {
								logger.info { "Direct msg, sending msg" }
								try {
									val privateChannel = member.user.openPrivateChannel().await()

									privateChannel.sendMessage(message).await()

									val shouldNotifyThatUserCanDisable = previousLevel % 10

									if (shouldNotifyThatUserCanDisable == 0) {
										privateChannel.sendMessage(locale["modules.levelUp.howToDisableLevelNotifications", "`${guild.name.stripCodeMarks()}`", "`xpnotifications`", Emotes.LORI_YAY.toString()]).await()
									}
								} catch (e: Exception) {
									logger.warn { "Error while sending DM to ${event.author} due to level up ($previousLevel -> $newLevel)"}
								}
							}
						}
						LevelUpAnnouncementType.DIFFERENT_CHANNEL -> {
							logger.info { "Diff channel, sending msg" }
							val channelId = announcement[LevelAnnouncementConfigs.channelId]

							if (channelId != null) {
								val channel = guild.getTextChannelById(channelId)

								channel?.sendMessage(
										message
								)?.queue()
							}
						}
					}
				}
			}
		}
	}
}