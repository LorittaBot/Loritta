package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import net.dv8tion.jda.api.Permission
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build<Long, List<String>>().asMap()
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

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val message = event.message
		val guild = message.guild
		val inviteBlockerConfig = serverConfig.inviteBlockerConfig

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

		if (validMatchers.isEmpty())
			return false

		// Se existe algum link na mensagem...
		val whitelisted = mutableListOf<String>()
		whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

		val callback = suspend callback@ {
			val jobs = mutableListOf<Deferred<Boolean>>()

			for (matcher in validMatchers) {
				val urls = mutableSetOf<String>()
				while (matcher.find()) {
					var url = matcher.group()
					if (url.startsWith("discord.gg")) {
						url = "discord.gg" + matcher.group(1).replace(".", "")
					}
					urls.add(url)
				}

				for (url in urls) {
					jobs.add(
							GlobalScope.async(loritta.coroutineDispatcher) {
								// Isto provavelmente jamais irá acontecer *exceto* se a coroutine realmente demorar muito para iniciar
								// Mas, caso as leis da natureza mudem, está aqui uma pequena verificação
								if (!isActive)
									return@async false

								val inviteId = if (!detectedInviteLinks.contains(url)) { // Se não está no cache, vamos verificar!
									val inviteId = MiscUtils.getInviteId("http://$url")
											?: run { if (isActive) MiscUtils.getInviteId("https://$url") else return@async false } // Vamos evitar verificações inúteis, certo?
											?: return@async false // Não é um convite! vlw flw fui

									if (!isActive)
										return@async false

									detectedInviteLinks[url] = inviteId
									inviteId
								} else {
									detectedInviteLinks[url]!!
								}

								if (whitelisted.contains(inviteId))
									return@async false

								jobs.forEach {
									if (it != this) { // Se a gente cancelar o atual, vai dar problema ao pegar o resultado depois
										it.cancel()
									}
								}

								if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
									message.delete().queue()

								if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty() && message.textChannel.canTalk()) {
									val toBeSent = MessageUtils.generateMessage(inviteBlockerConfig.warnMessage, listOf(message.author, guild), guild)
											?: return@async false

									message.textChannel.sendMessage(toBeSent).queue()

									return@async true
								}
								return@async false
							}
					)
				}

				jobs.onEach {
					try {
						if (it.await())
							return@callback true
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
				return@callback false
			}
			return@callback false
		}

		// Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
		// necessário, e, ao pegar, vamos guardar no cache de invites
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

		val result = callback.invoke()

		return result
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