package com.mrpowergamerbr.loritta.utils.modules

import com.github.kevinsawicki.http.HttpRequest
import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object InviteLinkModule {
	val cachedInviteLinks = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<String, List<String>>().asMap()

	fun checkForInviteLinks(message: Message, guild: Guild, lorittaProfile: GuildLorittaUser, permissionsConfig: PermissionsConfig, inviteBlockerConfig: InviteBlockerConfig): Boolean {
		if (inviteBlockerConfig.whitelistedChannels.contains(message.channel.id))
			return false

		if (lorittaProfile.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

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