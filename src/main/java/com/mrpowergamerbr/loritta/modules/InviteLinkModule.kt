package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build<String, List<String>>().asMap()
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (!serverConfig.inviteBlockerConfig.isEnabled)
			return false

		if (serverConfig.inviteBlockerConfig.whitelistedChannels.contains(event.channel.id))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
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
		for (embed in embeds) {
			val descriptionMatcher = getMatcherIfHasInviteLink(embed.description)
			if (descriptionMatcher != null)
				validMatchers.add(descriptionMatcher)

			for (field in embed.fields) {
				val fieldMatcher = getMatcherIfHasInviteLink(field.value)
				if (fieldMatcher != null)
					validMatchers.add(fieldMatcher)
			}
		}

		// Se existe algum link na mensagem...
		if (validMatchers.isNotEmpty()) {
			val whitelisted = mutableListOf<String>()
			whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

			val callback = callback@ {
				val jobs = mutableListOf<Deferred<Boolean>>()

				for (matcher in validMatchers) {
					while (matcher.find()) {
						var url = matcher.group()
						if (url.contains("discord") && url.contains("gg")) {
							url = "discord.gg" + matcher.group(1).replace(".", "")
						}

						jobs.add(
								async(loritta.coroutineDispatcher) {
									val inviteId = MiscUtils.getInviteId("http://$url")
											?: MiscUtils.getInviteId("https://$url")

									if (inviteId != null) { // INVITES DO DISCORD
										if (inviteId == "attachments" || inviteId == "forums")
											return@async false

										if (whitelisted.contains(inviteId))
											return@async false

										if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
											message.delete().queue()

										if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty() && message.textChannel.canTalk()) {
											val toBeSent = MessageUtils.generateMessage(inviteBlockerConfig.warnMessage, listOf(message.author, guild), guild)
													?: return@async false

											message.textChannel.sendMessage(toBeSent).queue()
										}
										return@async true
									}
									return@async false
								}
						)
					}

					runBlocking {
						jobs.forEach {
							if (it.await()) // true = Sim, tinha um invite
								return@forEach
						}
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
							launch(loritta.coroutineDispatcher) {
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

			launch(loritta.coroutineDispatcher) {
				callback.invoke()
			}
		}

		return false
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