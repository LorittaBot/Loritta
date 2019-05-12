package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.*
import java.util.concurrent.TimeUnit

class AutomodModule : MessageReceivedModule {
	companion object {
		val MESSAGES  = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build<String, Queue<Message>>().asMap()
		const val FRESH_ACCOUNT_TIMEOUT = 604_800_000L
		var ANTIRAID_ENABLED = true
		var SIMILAR_MESSAGE_MULTIPLIER = 0.0020
		var SIMILARITY_THRESHOLD = 7
		var IN_ROW_SAME_USER_SIMILAR_SCORE = 0.064
		var IN_ROW_DIFFERENT_USER_SIMILAR_SCORE = 0.056
		var DISTANCE_MULTIPLIER = 0.02
		var ATTACHED_IMAGE_SCORE = 0.015
		var SAME_LINK_SCORE = 0.005
		var SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER = 0.024
		var NO_AVATAR_SCORE = 0.04
		var MUTUAL_GUILDS_MULTIPLIER = 0.01
		var FRESH_ACCOUNT_DISCORD_MULTIPLIER = 0.00000000004
		var FRESH_ACCOUNT_JOINED_MULTIPLIER =  0.00000000013
		var QUEUE_SIZE = 50
		var BAN_THRESHOLD = 0.75

		val COMMON_EMOTES = listOf(
				";-;",
				";w;",
				"uwu",
				"owo",
				"-.-",
				"'-'",
				"'='",
				";=;",
				"-w-",
				"e.e",
				"e_e",
				"p-p",
				"q-q",
				"p-q",
				"q-p",
				"1",
				"2",
				"3",
				"4",
				"5",
				"6",
				"7",
				"8",
				"9",
				"10",
				"oi",
				"olá",
				"oie",
				"oin",
				"eu"
		)

		private val logger = KotlinLogging.logger {}
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_AUTO_MOD))
			return false

		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val message = event.message
		val textChannelConfig = serverConfig.getTextChannelConfig(message.channel.id)

		val automodConfig = textChannelConfig.automodConfig
		val automodCaps = automodConfig.automodCaps
		val automodSelfEmbed = automodConfig.automodSelfEmbed

		if (ANTIRAID_ENABLED && (loritta.discordConfig.antiRaidIds.contains(event.channel.id)) && loritta.config.loritta.environment == EnvironmentType.CANARY) {
			val messages = MESSAGES.getOrPut(event.textChannel!!.id) { Queues.synchronizedQueue(EvictingQueue.create<Message>(50)) }

			fun calculateRaidingPercentage(wrapper: Message): Double {
				var content = wrapper.contentRaw.toLowerCase()
				for (emote in AutomodModule.COMMON_EMOTES)
					content = content.replace(emote, "")

				val pattern = Constants.HTTP_URL_PATTERN
				val matcher = pattern.matcher(wrapper.contentRaw)

				val urlsDetected = mutableSetOf<String>()

				while (matcher.find())
					urlsDetected.add(matcher.group(0))

				val raider = wrapper.author
				var raidingPercentage = 0.0

				val verySimilarMessages = mutableListOf<Message>()
				var streamFloodCounter = 0

				messageLoop@for ((index, message) in messages.reversed().withIndex()) {
					val distanceMultiplier = ((AutomodModule.QUEUE_SIZE - index) * AutomodModule.DISTANCE_MULTIPLIER)
					if (message.contentRaw.isNotBlank()) {
						var compareContent = message.contentRaw.toLowerCase()
						for (emote in AutomodModule.COMMON_EMOTES)
							compareContent = compareContent.replace(emote, "")
						val contentIsBlank = compareContent.isBlank()
						val withoutEmoteBlankMultiplier = if (contentIsBlank) 0.3 else 1.0

						if (0 > streamFloodCounter)
							streamFloodCounter = 0

						val isStreamFlood = 3 > streamFloodCounter

						val threshold = LevenshteinDistance.getDefaultInstance().apply(compareContent.toLowerCase(), content.toLowerCase())

						if (3 >= threshold && wrapper.author.id == message.author.id) { // Vamos melhorar caso exista alguns "one person raider"
							verySimilarMessages.add(message)
						}

						if (5 >= threshold && isStreamFlood) { // Vamos aumentar os pontos caso sejam mensagens parecidas em seguida
							// threshold = 0..5
							// vamos aumentar as chances caso o conteúdo seja similar
							// 0 == * 1
							// 1 == * 0.75
							// etc
							// 5 - 0 = 5
							// 5 - 1 = 4
							val similarityMultiplier = (5 - Math.min(5, threshold))

							raidingPercentage += if (wrapper.author.id == message.author.id) {
								AutomodModule.IN_ROW_SAME_USER_SIMILAR_SCORE
							} else {
								AutomodModule.IN_ROW_DIFFERENT_USER_SIMILAR_SCORE
							} * distanceMultiplier * withoutEmoteBlankMultiplier * (similarityMultiplier * 0.2)

							// analysis(analysis, "+ Stream Flood (mesmo usuário: ${(wrapper.author.id == message.author.id)}) - Valor atual é $raidingPercentage")

							streamFloodCounter--
						} else {
							streamFloodCounter++
						}

						val similarMessageScore = distanceMultiplier * AutomodModule.SIMILAR_MESSAGE_MULTIPLIER * (Math.max(0, AutomodModule.SIMILARITY_THRESHOLD - threshold))
						raidingPercentage += similarMessageScore
					}

					if (wrapper.attachments.isNotEmpty() && message.attachments.isNotEmpty()) {
						raidingPercentage += AutomodModule.ATTACHED_IMAGE_SCORE * distanceMultiplier
						// analysis(analysis, "+ Possui attachments ~ ${AutomodModule.ATTACHED_IMAGE_SCORE} - Valor atual é $raidingPercentage")
						// println(">>> ${wrapper.author.id}: ATTACHED_IMAGE_SCORE ${raidingPercentage}")
					}

					val matcher2 = pattern.matcher(wrapper.contentRaw)

					while (matcher2.find()) {
						if (urlsDetected.contains(matcher2.group(0))) {
							// analysis(analysis, "+ Mesmo link ~ ${AutomodModule.SAME_LINK_SCORE} - Valor atual é $raidingPercentage")
							raidingPercentage += distanceMultiplier * AutomodModule.SAME_LINK_SCORE
							continue@messageLoop
						}
					}
				}

				val similarSameAuthorScore = AutomodModule.SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER * verySimilarMessages.size
				// analysis(analysis, "+ similarSameAuthorScore é $similarSameAuthorScore - Valor atual é $raidingPercentage")
				raidingPercentage += similarSameAuthorScore

				// Caso o usuário não tenha avatar
				if (wrapper.author.avatarUrl == null) {
					raidingPercentage += AutomodModule.NO_AVATAR_SCORE
					// analysis(analysis, "+ Usuário não possui avatar, então iremos adicionar ${AutomodModule.NO_AVATAR_SCORE} a porcentagem - Valor atual é $raidingPercentage")
				}

				// Caso o usuário esteja em poucos servidores compartilhados, a chance de ser raider é maior
				val nonMutualGuildsScore = AutomodModule.MUTUAL_GUILDS_MULTIPLIER * Math.max(5 - raider.mutualGuilds.size, 1)
				// analysis(analysis, "+ nonMutualGuildsScore é $nonMutualGuildsScore - Valor atual é $raidingPercentage")
				raidingPercentage += nonMutualGuildsScore

				// Conta nova no Discord
				val newAccountScore = AutomodModule.FRESH_ACCOUNT_DISCORD_MULTIPLIER * Math.max(0, AutomodModule.FRESH_ACCOUNT_TIMEOUT - (System.currentTimeMillis() - wrapper.author.timeCreated.toInstant().toEpochMilli()))
				// analysis(analysis, "+ newAccountScore é $nonMutualGuildsScore - Valor atual é $raidingPercentage")
				raidingPercentage += newAccountScore

				// Conta nova que entrou no servidor
				val member = event.member
				if (member != null) {
					val recentlyJoinedScore = AutomodModule.FRESH_ACCOUNT_JOINED_MULTIPLIER * Math.max(0, AutomodModule.FRESH_ACCOUNT_TIMEOUT - (System.currentTimeMillis() - member.timeJoined.toInstant().toEpochMilli()))
					// analysis(analysis, "+ recentlyJoinedScore é $recentlyJoinedScore - Valor atual é $raidingPercentage")
					raidingPercentage += recentlyJoinedScore
				}

				return raidingPercentage
			}

			val raidingPercentage = calculateRaidingPercentage(event.message)
			logger.info("[${event.guild!!.name} -> ${event.channel.name}] ${event.author.id} (${raidingPercentage}% chance de ser raider: ${event.message.contentRaw}")

			if (raidingPercentage >= 0.5) {
				logger.warn("[${event.guild.name} -> ${event.channel.name}] ${event.author.id} (${raidingPercentage}% chance de ser raider (CHANCE ALTA DEMAIS!): ${event.message.contentRaw}")
			}
			if (raidingPercentage >= BAN_THRESHOLD) {
				logger.info("Aplicando punimentos em ${event.guild.name} -> ${event.channel.name}, causado por ${event.author.id}!")
				synchronized(event.guild) {
					val alreadyBanned = mutableListOf<User>()

					for (storedMessage in messages) {
						if (!event.guild.isMember(event.author) || alreadyBanned.contains(storedMessage.author)) // O usuário já pode estar banido
							continue

						val percentage = calculateRaidingPercentage(storedMessage)

						if (percentage >= BAN_THRESHOLD) {
							alreadyBanned.add(storedMessage.author)
							if (event.guild.selfMember.canInteract(event.member!!)) {
								logger.info("Punindo ${storedMessage.author.id} em ${event.guild.name} -> ${event.channel.name} por tentativa de raid! ($percentage%)!")
								BanCommand.ban(serverConfig, event.guild, event.guild.selfMember.user, locale, storedMessage.author, "Tentativa de Raid (Spam/Flood)! Que feio, para que fazer isto? Vá procurar algo melhor para fazer em vez de incomodar outros servidores. ᕙ(⇀‸↼‶)ᕗ", false, 7)
							}
						}
					}

					if (!event.guild.isMember(event.author) || alreadyBanned.contains(event.author)) // O usuário já pode estar banido
						return true

					if (event.guild.selfMember.canInteract(event.member!!)) {
						logger.info("Punindo ${event.author.id} em ${event.guild.name} -> ${event.channel.name} por tentativa de raid! ($raidingPercentage%)!")
						BanCommand.ban(serverConfig, event.guild, event.guild.selfMember.user, locale, event.author, "Tentativa de Raid (Spam/Flood)! Que feio, para que fazer isto? Vá procurar algo melhor para fazer em vez de incomodar outros servidores. ᕙ(⇀‸↼‶)ᕗ", false, 7)
					}
				}
				return true
			}

			messages.add(event.message)
		}

		if (automodCaps.isEnabled) {
			val content = message.contentRaw.replace(" ", "")
			val capsThreshold = automodCaps.capsThreshold

			val length = content.length.toDouble()
			if (length >= automodCaps.lengthThreshold) {
				val caps = content.count { it.isUpperCase() }.toDouble()

				val percentage = (caps / length) * 100

				if (percentage >= capsThreshold) {
					if (automodCaps.deleteMessage && event.guild!!.selfMember.hasPermission(event.textChannel!!, Permission.MESSAGE_MANAGE))
						message.delete().queue()

					if (automodCaps.replyToUser && message.textChannel.canTalk()) {
						message.channel.sendMessage(
								MessageUtils.generateMessage(automodCaps.replyMessage,
										listOf(event.guild!!, event.member!!),
										event.guild
								)!!
						).queue {
							if (automodCaps.enableMessageTimeout && it.guild.selfMember.hasPermission(event.textChannel!!, Permission.MESSAGE_MANAGE)) {
								val delay = Math.min(automodCaps.messageTimeout * 1000, 60000)
								it.delete().queueAfter(delay.toLong(), TimeUnit.MILLISECONDS)
							}
						}
					}

					return true
				}
			}
		}

		/* if (automodSelfEmbed.isEnabled) {
			if (message.embeds.isNotEmpty()) {
				if (automodSelfEmbed.deleteMessage)
					message.delete().queue()

				if (automodSelfEmbed.replyToUser) {
					val message = message.channel.sendMessage(MessageUtils.generateMessage(automodSelfEmbed.replyMessage, listOf(event.guild!!, event.member!!), event.guild)).complete()

					if (automodSelfEmbed.enableMessageTimeout) {
						var delay = Math.min(automodSelfEmbed.messageTimeout * 1000, 60000)
						Thread.sleep(delay.toLong())
						message.delete().queue()
					}
				}
			}
		} */
		return false
	}
}