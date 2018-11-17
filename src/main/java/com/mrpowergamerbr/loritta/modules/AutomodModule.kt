package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.*
import java.util.concurrent.TimeUnit

class AutomodModule : MessageReceivedModule {
	companion object {
		val MESSAGES  = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build<String, Queue<Message>>().asMap()
		const val FRESH_ACCOUNT_TIMEOUT = 604_800_000L
		var ANTIRAID_ENABLED = true
		var SIMILAR_MESSAGE_MULTIPLIER = 0.0025
		var SIMILARITY_THRESHOLD = 7
		var ATTACHED_IMAGE_SCORE = 0.005
		var SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER = 0.015
		var NO_AVATAR_SCORE = 0.15
		var MUTUAL_GUILDS_MULTIPLIER = 0.01
		var FRESH_ACCOUNT_DISCORD_MULTIPLIER = 0.01
		var FRESH_ACCOUNT_JOINED_MULTIPLIER = 0.275
		var BAN_THRESHOLD = 0.75
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_AUTO_MOD))
			return false

		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val message = event.message
		val textChannelConfig = serverConfig.getTextChannelConfig(message.channel.id)

		val automodConfig = textChannelConfig.automodConfig
		val automodCaps = automodConfig.automodCaps
		val automodSelfEmbed = automodConfig.automodSelfEmbed

		if (ANTIRAID_ENABLED && (event.guild!!.id == "268353819409252352" || event.channel.id == "297732013006389252" || event.channel.id == "398987569485971466") && Loritta.config.environment == EnvironmentType.CANARY) {
			val messages = MESSAGES.getOrPut(event.textChannel!!.id) { Queues.synchronizedQueue(EvictingQueue.create<Message>(50)) }

			fun calculateRaidingPercentage(wrapper: Message): Double {
				// println(wrapper.author.id + ": (original message is ${wrapper.content}")
				val raider = wrapper.author
				var raidingPercentage = 0.0

				val verySimilarMessages = mutableListOf<Message>()

				for (message in messages) {
					// println(message.content + " -- " + wrapper.content)
					val threshold = LevenshteinDistance.getDefaultInstance().apply(message.contentRaw.toLowerCase(), wrapper.contentRaw.toLowerCase())

					if (3 >= threshold && wrapper.author.id == message.author.id) { // Vamos melhorar caso exista alguns "one person raider"
						verySimilarMessages.add(message)
					}

					// println(Math.max(0, 25 - threshold))
					raidingPercentage += SIMILAR_MESSAGE_MULTIPLIER * (Math.max(0, SIMILARITY_THRESHOLD - threshold))
					// raidingPercentage += 0.005 * Math.max(message.contentRaw.length - 500, 0)
					// val diff = wrapper.sentAt - message.sentAt
					// raidingPercentage += 0.00008 * Math.max(0, (1250 - diff))

					if (wrapper.attachments.isNotEmpty() && message.attachments.isNotEmpty()) {
						raidingPercentage += ATTACHED_IMAGE_SCORE
					}
				}

				raidingPercentage += SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER * verySimilarMessages.size

				if (wrapper.author.avatarUrl == null) {
					raidingPercentage += NO_AVATAR_SCORE
				}

				// Caso o usuário esteja em poucos servidores compartilhados, a chance de ser raider é maior
				raidingPercentage += MUTUAL_GUILDS_MULTIPLIER * Math.max(5 - raider.mutualGuilds.size, 1)
				raidingPercentage += FRESH_ACCOUNT_DISCORD_MULTIPLIER * Math.max(FRESH_ACCOUNT_TIMEOUT - (wrapper.author.creationTime.toInstant().toEpochMilli() - FRESH_ACCOUNT_TIMEOUT), 0)
				val member = wrapper.member
				if (member != null) {
					raidingPercentage += FRESH_ACCOUNT_JOINED_MULTIPLIER * Math.max(FRESH_ACCOUNT_TIMEOUT - (member.joinDate.toInstant().toEpochMilli() - FRESH_ACCOUNT_TIMEOUT), 0)
				}

				return raidingPercentage
			}

			val raidingPercentage = calculateRaidingPercentage(event.message)
			println("${event.author.id} (${raidingPercentage}% chance de ser raider ~ ${messages.toMutableList().size}): ${event.message.contentRaw}")

			if (raidingPercentage >= BAN_THRESHOLD) {
				println("Applying punishments to all involved!")
				val alreadyBanned = mutableListOf<User>()

				for (storedMessage in messages) {
					if (!event.guild.isMember(event.author) || alreadyBanned.contains(storedMessage.author)) // O usuário já pode estar banido
						continue

					val percentage = calculateRaidingPercentage(storedMessage)

					if (percentage >= BAN_THRESHOLD) {
						alreadyBanned.add(storedMessage.author)
						BanCommand.ban(serverConfig, event.guild, event.guild.selfMember.user, locale, storedMessage.author, "Tentativa de Raid! (Isto é experimental, caso você tenha sido banido sem querer, vá em https://loritta.website/support e entre de novo :3)", false, 7)
					}
				}

				if (!event.guild.isMember(event.author) || alreadyBanned.contains(event.author)) // O usuário já pode estar banido
					return true

				BanCommand.ban(serverConfig, event.guild, event.guild.selfMember.user, locale, event.author, "Tentativa de Raid! (Isto é experimental, caso você tenha sido banido sem querer, vá em https://loritta.website/support e entre de novo :3)", false, 7)
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
								)
						).queue {
							if (automodCaps.enableMessageTimeout && it.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
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