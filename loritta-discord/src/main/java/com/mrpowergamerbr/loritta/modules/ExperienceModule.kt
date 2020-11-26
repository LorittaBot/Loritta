package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.filterOnlyGiveableRoles
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.perfectdreams.loritta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ExperienceUtils
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
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

	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (!FeatureFlags.isEnabled("experience-gain"))
			return false

		// (copyright Loritta™)
		var newProfileXp = lorittaProfile?.xp ?: 0L
		val currentXp = newProfileXp
		val lastMessageSentAt = lorittaProfile?.lastMessageSentAt ?: 0L
		val currentLastMessageSentHash = lorittaProfile?.lastMessageSentHash ?: 0L
		var lastMessageSentHash: Int? = null
		val retrievedProfile by lazy { lorittaProfile ?: loritta.getOrCreateLorittaProfile(event.author.idLong) }

		// Do not give XP if the message contains a code block
		// Users would be able to gain a lot of experience by hidding text in the ```languageCodeHere section
		// So we need to ignore if the message contains code blocks
		if (event.message.contentRaw.contains("```"))
			return false

		// Primeiro iremos ver se a mensagem contém algo "interessante"
		if (event.message.contentStripped.length >= 5 && currentLastMessageSentHash != event.message.contentStripped.hashCode()) {
			// Primeiro iremos verificar se a mensagem é "válida"
			// 7 chars por millisegundo
			val calculatedMessageSpeed = event.message.contentStripped.toLowerCase().length.toDouble() / 7

			val diff = System.currentTimeMillis() - lastMessageSentAt

			if (diff > calculatedMessageSpeed * 1000) {
				val nonRepeatedCharsMessage = event.message.contentStripped.replace(Constants.REPEATING_CHARACTERS_REGEX, "$1")

				if (nonRepeatedCharsMessage.length >= 12) {
					val gainedXp = Math.min(35, Loritta.RANDOM.nextInt(Math.max(1, nonRepeatedCharsMessage.length / 7), (Math.max(2, nonRepeatedCharsMessage.length / 4))))

					var globalGainedXp = gainedXp

					val donatorPaid = loritta.getActiveMoneyFromDonationsAsync(event.author.idLong)
					if (donatorPaid != 0.0) {
						val plan = ServerPremiumPlans.getPlanFromValue(donatorPaid)
						globalGainedXp = (globalGainedXp * plan.globalXpMultiplier).toInt()
					}

					newProfileXp = currentXp + globalGainedXp
					lastMessageSentHash = event.message.contentStripped.hashCode()

					val profile = serverConfig.getUserData(event.author.idLong)

					if (FeatureFlags.isEnabled("experience-gain-locally")) {
						handleLocalExperience(event, retrievedProfile, serverConfig, profile, gainedXp, locale)
					}
				}
			}
		}

		if (lastMessageSentHash != null && currentXp != newProfileXp) {
			val mutex = mutexes.getOrPut(event.author.idLong) { Mutex() }

			if (FeatureFlags.isEnabled("experience-gain-globally")) {
				mutex.withLock {
					loritta.newSuspendedTransaction {
						retrievedProfile.lastMessageSentHash = lastMessageSentHash
						retrievedProfile.xp = newProfileXp
						retrievedProfile.lastMessageSentAt = System.currentTimeMillis()
					}
				}
			}
		}
		return false
	}

	suspend fun handleLocalExperience(event: LorittaMessageEvent, profile: Profile, serverConfig: ServerConfig, guildProfile: GuildProfile, gainedXp: Int, locale: BaseLocale) {
		val mutex = mutexes.getOrPut(event.author.idLong) { Mutex() }

		val guild = event.guild!!
		val member = event.member!!

		val levelConfig = serverConfig.getCachedOrRetreiveFromDatabase<LevelConfig?>(ServerConfig::levelConfig)

		// We need to include the publicRole because member.roles does NOT contain the "@everyone" role
		val memberRolesIds = member.roles.map { it.idLong } + event.guild.publicRole.idLong

		if (levelConfig != null) {
			logger.info { "Level Config isn't null in $guild" }

			val noXpRoles = levelConfig.noXpRoles

			if (memberRolesIds.any { it in noXpRoles })
				return

			val noXpChannels = levelConfig.noXpChannels

			if (event.channel.idLong in noXpChannels)
				return
		}

		val (previousLevel, previousXp) = guildProfile.getCurrentLevel()

		val customRoleRates = loritta.newSuspendedTransaction {
			ExperienceRoleRates.select {
				ExperienceRoleRates.guildId eq event.guild.idLong and
						(ExperienceRoleRates.role inList memberRolesIds)
			}.orderBy(ExperienceRoleRates.rate, SortOrder.DESC)
					.firstOrNull()
		}

		val rate = customRoleRates?.getOrNull(ExperienceRoleRates.rate) ?: 1.0

		mutex.withLock {
			loritta.newSuspendedTransaction {
				guildProfile.xp += (gainedXp * rate).toLong()
			}
		}

		val (newLevel, newXp) = guildProfile.getCurrentLevel()

		var receivedNewRoles = false
		val givenNewRoles = mutableSetOf<Role>()

		if (guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) {
			val configs = loritta.newSuspendedTransaction {
				RolesByExperience.select {
					RolesByExperience.guildId eq guild.idLong
				}.toMutableList()
			}

			val matched = configs.filter { guildProfile.xp >= it[RolesByExperience.requiredExperience] }
					.sortedByDescending { it[RolesByExperience.requiredExperience] }

			if (matched.isNotEmpty()) {
				val guildRoles = matched.flatMap { it[RolesByExperience.roles]
						.mapNotNull { guild.getRoleById(it) } }
						.distinct()
						.filterOnlyGiveableRoles()
						.toList()

				if (guildRoles.isNotEmpty()) {
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
		}

		if (previousLevel != newLevel && levelConfig != null) {
			logger.info { "Notifying about level up from $previousLevel -> $newLevel; level config is $levelConfig"}

			val announcements = loritta.newSuspendedTransaction {
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
								guild,
								event.channel
						),
						guild,
						mutableMapOf(
								"previous-level" to previousLevel.toString(),
								"previous-xp" to previousXp.toString(),
								"new-roles" to givenNewRoles.joinToString(transform = { it.asMention })
						).apply {
							putAll(
									ExperienceUtils.getExperienceCustomTokens(
											serverConfig,
											event.member
									)
							)
						}
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
							val profileSettings = loritta.newSuspendedTransaction {
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