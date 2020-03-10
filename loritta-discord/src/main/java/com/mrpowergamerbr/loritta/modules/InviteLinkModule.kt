package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build<Long, List<String>>().asMap()
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (!legacyServerConfig.inviteBlockerConfig.isEnabled)
			return false

		if (legacyServerConfig.inviteBlockerConfig.whitelistedChannels.contains(event.channel.id))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val message = event.message
		val guild = message.guild
		val inviteBlockerConfig = legacyServerConfig.inviteBlockerConfig

		val content = message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")

		val validMatchers = mutableListOf<Matcher>()
		val contentMatcher = getMatcherIfHasInviteLink(content)
		if (contentMatcher != null)
			validMatchers.add(contentMatcher)

		val embeds = message.embeds
		if (!isYouTubeLink(content)) {
			for (embed in embeds) {
				val descriptionMatcher = getMatcherIfHasInviteLink(embed.description)
				if (descriptionMatcher != null)
					validMatchers.add(descriptionMatcher)

				val titleMatcher = getMatcherIfHasInviteLink(embed.title)
				if (titleMatcher != null)
					validMatchers.add(titleMatcher)

				val urlMatcher = getMatcherIfHasInviteLink(embed.url)
				if (urlMatcher != null)
					validMatchers.add(urlMatcher)

				val footerMatcher = getMatcherIfHasInviteLink(embed.footer?.text)
				if (footerMatcher != null)
					validMatchers.add(footerMatcher)

				val authorNameMatcher = getMatcherIfHasInviteLink(embed.author?.name)
				if (authorNameMatcher != null)
					validMatchers.add(authorNameMatcher)

				val authorUrlMatcher = getMatcherIfHasInviteLink(embed.author?.url)
				if (authorUrlMatcher != null)
					validMatchers.add(authorUrlMatcher)

				for (field in embed.fields) {
					val fieldMatcher = getMatcherIfHasInviteLink(field.value)
					if (fieldMatcher != null)
						validMatchers.add(fieldMatcher)
				}
			}
		}

		// Se existe algum link na mensagem...
		if (validMatchers.isEmpty())
			return false

		// Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
		// necessário, e, ao pegar, vamos guardar no cache de invites
		val whitelisted = mutableListOf<String>()
		if (inviteBlockerConfig.whitelistServerInvites) {
			guild.vanityCode?.let { whitelisted.add(it) }

			if (!cachedInviteLinks.containsKey(guild.idLong) && guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
				val invites = guild.retrieveInvites().await()
				val codes = invites.map { it.code }
				cachedInviteLinks[guild.idLong] = codes
			}

			cachedInviteLinks[guild.idLong]?.forEach {
				whitelisted.add(it)
			}
		}

		whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

		for (matcher in validMatchers) {
			val urls = mutableSetOf<String>()
			while (matcher.find()) {
				var url = matcher.group()
				if (url.startsWith("discord.gg", true)) {
					url = "discord.gg" + matcher.group(1).replace(".", "")
				}
				urls.add(url)
			}

			for (url in urls) {
				val inviteId = MiscUtils.getInviteId(url) ?: continue

				if (whitelisted.contains(inviteId))
					continue

				if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
					message.delete().queue()

				if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty() && message.textChannel.canTalk()) {
					val toBeSent = MessageUtils.generateMessage(inviteBlockerConfig.warnMessage, listOf(message.author, guild), guild)
							?: return true

					message.textChannel.sendMessage(toBeSent).queue()

					return true
				}
			}
		}
		return false
	}

	fun isYouTubeLink(content: String?): Boolean {
		if (content.isNullOrBlank())
			return false

		val pattern = Constants.URL_PATTERN
		val matcher = pattern.matcher(content)
		if (matcher.find()) {
			val everything = matcher.group(0)
			val afterSlash = matcher.group(1)
			val uri = everything.replace(afterSlash, "")
			return uri.endsWith("youtube.com") || uri.endsWith("youtu.be")
		} else {
			return false
		}
	}

	fun getMatcherIfHasInviteLink(content: String?): Matcher? {
		if (content.isNullOrBlank())
			return null

		val pattern = Constants.URL_PATTERN
		val matcher = pattern.matcher(content)
		if (matcher.find()) {
			matcher.reset()
			return matcher
		} else {
			return null
		}
	}
}