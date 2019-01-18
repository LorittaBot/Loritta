package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build<String, List<String>>().asMap()
		val detectedInviteLinks = Caffeine.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build<String, String>().asMap()
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		if (!serverConfig.inviteBlockerConfig.isEnabled)
			return false

		if (serverConfig.inviteBlockerConfig.whitelistedChannels.contains(event.channel.id))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val message = event.message
		val guild = message.guild
		val inviteBlockerConfig = serverConfig.inviteBlockerConfig

		val content = message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")
				.toLowerCase()

		val validMatchers = mutableListOf<Matcher>()
		val contentMatcher = getMatcherIfHasInviteLink(content)
		if (contentMatcher != null)
			validMatchers.add(contentMatcher)

		val embeds = message.embeds
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

		if (validMatchers.isEmpty())
			return false

		// Se existe algum link na mensagem...
		val whitelisted = mutableListOf<String>()
		whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

		val callback = callback@ {
			val jobs = mutableListOf<Job>()

			for (matcher in validMatchers) {
				val urls = mutableSetOf<String>()
				while (matcher.find()) {
					var url = matcher.group()
					if (url.contains("discord") && url.contains("gg")) {
						url = "discord.gg" + matcher.group(1).replace(".", "")
					}
					urls.add(url)
				}

				for (url in urls) {
					jobs.add(
							GlobalScope.launch(loritta.coroutineDispatcher) {
								// Isto provavelmente jamais irá acontecer *exceto* se a coroutine realmente demorar muito para iniciar
								// Mas, caso as leis da natureza mudem, está aqui uma pequena verificação
								if (!isActive)
									return@launch

								val inviteId = if (!detectedInviteLinks.contains(url)) { // Se não está no cache, vamos verificar!
									val inviteId = MiscUtils.getInviteId("http://$url")
											?: run { if (isActive) MiscUtils.getInviteId("https://$url") else return@launch } // Vamos evitar verificações inúteis, certo?
											?: return@launch // Não é um convite! vlw flw fui

									if (!isActive)
										return@launch

									detectedInviteLinks[url] = inviteId
									inviteId
								} else {
									detectedInviteLinks[url]!!
								}

								if (inviteId == "attachments" || inviteId == "forums")
									return@launch

								if (whitelisted.contains(inviteId))
									return@launch

								jobs.forEach { it.cancel() }

								if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
									message.delete().queue()

								if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty() && message.textChannel.canTalk()) {
									val toBeSent = MessageUtils.generateMessage(inviteBlockerConfig.warnMessage, listOf(message.author, guild), guild)
											?: return@launch

									message.textChannel.sendMessage(toBeSent).queue()
								}
							}
					)
				}
			}
		}

		// Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
		// necessário, e, ao pegar, vamos guardar no cache de invites
		if (inviteBlockerConfig.whitelistServerInvites) {
			if (!cachedInviteLinks.containsKey(guild.id)) {
				if (guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
					guild.invites.queue {
						val codes = it.map { it.code }
						cachedInviteLinks.put(guild.id, codes)
						codes.forEach {
							whitelisted.add(it)
						}
						GlobalScope.launch(loritta.coroutineDispatcher) {
							callback.invoke()
						}
					}
					return false
				}
			} else {
				cachedInviteLinks[guild.id]?.forEach {
					whitelisted.add(it)
				}
			}
		}

		GlobalScope.launch(loritta.coroutineDispatcher) {
			callback.invoke()
		}

		return false
	}

	fun getMatcherIfHasInviteLink(content: String?): Matcher? {
		if (content.isNullOrBlank())
			return null

		val pattern = Constants.URL_PATTERN
		val matcher = pattern.matcher(content)
		if (matcher.find()) {
			val everything = matcher.group(0)
			val afterSlash = matcher.group(1)
			val uri = everything.replace(afterSlash, "")
			if (uri.endsWith("youtube.com") || uri.endsWith("youtu.be"))
				return null

			matcher.reset()
			return matcher
		} else {
			return null
		}
	}
}