package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Emote
import java.time.Instant
import java.time.ZoneId

class UserInfoCommand : AbstractCommand("userinfo", listOf("memberinfo"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
						LoriReply(
								locale["USERINFO_UnknownUser", context.args[0].stripCodeMarks()],
								Constants.ERROR
						)
				)
				return
			}
			user = context.userHandle
		}

		val member = if (context.guild.isMember(user)) {
			context.guild.getMember(user)
		} else {
			null
		}

		val embed = EmbedBuilder()

		embed.apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null) {
				nickname = member.effectiveName
			}

			setTitle("<:discord:314003252830011395> $nickname", null)
			setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)

			val lorittaProfile = loritta.getLorittaProfileForUser(user.id)
			if (lorittaProfile.hidePreviousUsernames) {
				var alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n*${locale["USERINFO_PrivacyOn"]}*"
				setDescription(alsoKnownAs)
			} else {
				val usernameChanges = lorittaProfile.usernameChanges
				if (usernameChanges.isEmpty()) {
					usernameChanges.add(LorittaProfile.UsernameChange(user.creationTime.toEpochSecond() * 1000, user.name, user.discriminator))
				}

				val sortedChanges = lorittaProfile.usernameChanges.sortedBy { it.changedAt }.toMutableList()

				if (sortedChanges[0].discriminator == user.discriminator && sortedChanges[0].username == user.name) {
					sortedChanges.removeAt(0)
				}

				if (sortedChanges.isNotEmpty()) {
					var alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n" + sortedChanges.joinToString(separator = "\n", transform = {
						"${it.username}#${it.discriminator} (" + Instant.ofEpochMilli(it.changedAt).atZone(ZoneId.systemDefault()).toOffsetDateTime().humanize(locale) + ")"
					})
					// Verificar tamanho do "alsoKnownAs" e, se necessário, cortar
					var alsoKnownAsLines = alsoKnownAs.split("\n").reversed()

					var aux = mutableListOf<String>()

					var length = 0
					for (line in alsoKnownAsLines) {
						if (length + line.length >= 2000) {
							break
						}
						aux.add(line)
						length += line.length
					}
					setDescription(aux.reversed().joinToString(separator = "\n"))
				}
			}

			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_TAG_DO_DISCORD"), "${user.name}#${user.discriminator}", true)
			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_ID_DO_DISCORD"), user.id, true)
			addField("\uD83D\uDCC5 " + context.locale.get("USERINFO_ACCOUNT_CREATED"), user.creationTime.humanize(locale), true)
			if (member != null)
				addField("\uD83C\uDF1F " + context.locale.get("USERINFO_ACCOUNT_JOINED"), member.joinDate.humanize(locale), true)

			val sharedServers = lorittaShards.getMutualGuilds(user)

			var servers = if (lorittaProfile.hideSharedServers) {
				"*${locale["USERINFO_PrivacyOn"]}*"
			} else {
				sharedServers.joinToString(separator = ", ", transform = { "${it.name}"})
			}

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "...";
			}

			embed.addField("\uD83C\uDF0E " + context.locale["USERINFO_SHARED_SERVERS"] + " (${sharedServers.size})", servers, true)
			if (member != null) {
				addField("\uD83D\uDCE1 " + context.locale["USERINFO_STATUS"], member.onlineStatus.name, true)

				val roles = member.roles.joinToString(separator = ", ", transform = { "${it.name}" });

				addField("\uD83D\uDCBC " + context.locale["USERINFO_ROLES"], if (roles.isNotEmpty()) roles else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			}

			val profile = loritta.getLorittaProfileForUser(user.id)

			val offset = Instant.ofEpochMilli(profile.lastMessageSent).atZone(ZoneId.systemDefault()).toOffsetDateTime();

			if (profile.lastMessageSent != 0L) {
				addField("\uD83D\uDC40 " + context.locale["USERINFO_LAST_SEEN"], offset.humanize(locale), true)
			}

			val favoriteEmotes = lorittaProfile.usedEmotes.entries.sortedByDescending { it.value }
			var emotes = mutableListOf<Emote>()

			for (favoriteEmote in favoriteEmotes) {
				val emote = lorittaShards.getEmoteById(favoriteEmote.key)
				if (emote != null)
					emotes.add(emote)
			}

			if (emotes.isNotEmpty())
				addField("<:lori_yum:414222275223617546> ${locale["USERINFO_FavoriteEmojis"]}", emotes.joinToString("", limit = 5, truncated = "", transform = { it.asMention }), true)

			embed.setFooter(locale["USERINFO_PrivacyInfo"], null)
		}

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}