package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.common.locale.BaseLocale
import org.jetbrains.exposed.sql.insert
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

class InviteLinkModule : MessageReceivedModule {
	companion object {
		val cachedInviteLinks = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).build<Long, List<String>>().asMap()
	}

	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val inviteBlockerConfig = serverConfig.getCachedOrRetreiveFromDatabase<InviteBlockerConfig?>(ServerConfig::inviteBlockerConfig)
				?: return false

		if (!inviteBlockerConfig.enabled)
			return false

		if (inviteBlockerConfig.whitelistedChannels.contains(event.channel.idLong))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

		return true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val message = event.message
		val guild = message.guild
		val inviteBlockerConfig = serverConfig.getCachedOrRetreiveFromDatabase<InviteBlockerConfig?>(ServerConfig::inviteBlockerConfig)
				?: return false

		val content = message.contentRaw
				// We need to strip the code marks to avoid this:
				// https://cdn.discordapp.com/attachments/513405772911345664/760887806191992893/invite-bug.png
				.stripCodeMarks()
				.replace("\u200B", "")
				// https://discord.gg\loritta is actually detected as https://discord.gg/loritta on Discord
				// So we are going to flip all \ to /
				.replace("\\", "/")
				// https://discord.gg//loritta is actually detected as https://discord.gg/loritta on Discord
				// (yes, two issues, wow)
				// So we are going to replace all /+ to /, so https://discord.gg//loritta becomes https://discord.gg/loritta
				.replace(Regex("/+"), "/")

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

		// whitelisted.addAll(inviteBlockerConfig.whitelistedIds)

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

				val warnMessage = inviteBlockerConfig.warnMessage

				if (inviteBlockerConfig.tellUser && !warnMessage.isNullOrEmpty() && message.textChannel.canTalk()) {
					if (event.member != null && event.member.hasPermission(Permission.MANAGE_SERVER)) {
						// Se a pessoa tiver permissão para ativar a permissão de convites, faça que a Loritta recomende que ative a permissão
						val topRole = event.member.roles.sortedByDescending { it.position }.firstOrNull { !it.isPublicRole }

						if (topRole != null) {
							val enableBypassMessage = message.textChannel.sendMessageAsync(
									listOf(
											LorittaReply(
													locale["modules.inviteBlocker.activateInviteBlockerBypass", topRole.asMention, Emotes.LORI_PAT],
													Emotes.LORI_SMILE
											),
											LorittaReply(
													locale["modules.inviteBlocker.howToReEnableLater", "<${loritta.instanceConfig.loritta.website.url}guild/${event.member.guild.idLong}/configure/permissions>"],
													Emotes.LORI_HM
											)
									).joinToString("\n") { it.build(JDAUser(event.member.user)) }
							)

							enableBypassMessage.onReactionAddByAuthor(event.author.idLong) {
								if (it.reactionEmote.id == (Emotes.LORI_PAT as DiscordEmote).id) {
									enableBypassMessage.removeAllFunctions()

									loritta.newSuspendedTransaction {
										ServerRolePermissions.insert {
											it[ServerRolePermissions.guild] = serverConfig.id
											it[ServerRolePermissions.roleId] = topRole.idLong
											it[ServerRolePermissions.permission] = LorittaPermission.ALLOW_INVITES
										}
									}

									// Because Loritta caches role permissions, we need to invalidate the current config to avoid cache inconsistencies.
									loritta.cachedServerConfigs.invalidate(serverConfig.id.value)

									message.textChannel.sendMessage(
											LorittaReply(
													locale["modules.inviteBlocker.bypassEnabled", topRole.asMention],
													Emotes.LORI_HAPPY
											).build(JDAUser(event.member.user))
									).queue()
								}
							}

							enableBypassMessage.addReaction((Emotes.LORI_PAT as DiscordEmote).reactionCode).queue()
						}
					}

					val toBeSent = MessageUtils.generateMessage(warnMessage, listOf(message.author, guild, message.channel), guild)
							?: return true

					message.textChannel.sendMessage(toBeSent).queue()
				}

				return true
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