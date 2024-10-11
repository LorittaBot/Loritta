package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.BanCommand
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.*
import java.util.concurrent.TimeUnit

class AutomodModule(val loritta: LorittaBot) : MessageReceivedModule {
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

	override suspend fun matches(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_AUTO_MOD))
			return false

		return true
	}

	override suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
		if (ANTIRAID_ENABLED && (loritta.config.loritta.antiRaidIds.contains(event.channel.idLong)) && loritta.config.loritta.environment == EnvironmentType.CANARY) {
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

				val settings = AdminUtils.retrieveModerationInfo(loritta, serverConfig)

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
								BanCommand.ban(loritta, i18nContext, settings, event.guild, event.guild.selfMember.user, locale, storedMessage.author, "Tentativa de Raid (Spam/Flood)! Que feio, para que fazer isto? Vá procurar algo melhor para fazer em vez de incomodar outros servidores. ᕙ(⇀‸↼‶)ᕗ", false, 7)
							}
						}
					}

					if (!event.guild.isMember(event.author) || alreadyBanned.contains(event.author)) // O usuário já pode estar banido
						return true

					if (event.guild.selfMember.canInteract(event.member!!)) {
						logger.info("Punindo ${event.author.id} em ${event.guild.name} -> ${event.channel.name} por tentativa de raid! ($raidingPercentage%)!")
						BanCommand.ban(loritta, i18nContext, settings, event.guild, event.guild.selfMember.user, locale, event.author, "Tentativa de Raid (Spam/Flood)! Que feio, para que fazer isto? Vá procurar algo melhor para fazer em vez de incomodar outros servidores. ᕙ(⇀‸↼‶)ᕗ", false, 7)
					}
				}
				return true
			}

			messages.add(event.message)
		}

		return false
	}
}