package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<String, List<String>>().asMap()
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
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

		val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,7}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(content)

		// Se existe algum link na mensagem...
		if (matcher.find()) {
			matcher.reset()

			val whitelisted = mutableListOf<String>()
			whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

			val callback = callback@ {
				while (matcher.find()) {
					var url = matcher.group()
					if (url.contains("discord") && url.contains("gg")) {
						url = "discord.gg" + matcher.group(1).replace(".", "")
					}

					val inviteId = MiscUtils.getInviteId("http://$url") ?: MiscUtils.getInviteId("https://$url")

					if (inviteId != null) { // INVITES DO DISCORD
						if (inviteId == "attachments" || inviteId == "forums")
							continue

						if (whitelisted.contains(inviteId))
							continue

						if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
							message.delete().queue()

						if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty() && message.textChannel.canTalk()) {
							val toBeSent = MessageUtils.generateMessage(inviteBlockerConfig.warnMessage, listOf(message.author, guild), guild) ?: return@callback

							message.textChannel.sendMessage(toBeSent).queue()

							return@callback
						}
					}
				}
			}

			// Para evitar que use a API do Discord para pegar os invites do servidor toda hora, nós iremos *apenas* pegar caso seja realmente
			// necessário, e, ao pegar, vamos guardar no cache de invites
			if (inviteBlockerConfig.whitelistServerInvites) {
				if (!cachedInviteLinks.containsKey(guild.id)) {
					if (guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
						guild.invites.queue({
							val codes = it.map { it.code }
							cachedInviteLinks.put(guild.id, codes)
							codes.forEach {
								whitelisted.add(it)
							}
							launch {
								callback.invoke()
							}
						})
						return false
					}
				} else {
					cachedInviteLinks[guild.id]?.forEach {
						whitelisted.add(it)
					}
				}
			}

			launch {
				callback.invoke()
			}
		}

		return false
	}
}