package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.dao.Profile
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User

object AdminUtils {
	fun sendSuspectInfo(user: User, profile: Profile?) {
		val channel = lorittaShards.getTextChannelById(Constants.SUSPECTS_CHANNEL)
		if (channel != null && (profile == null || (profile.lastMessageSentAt == 0L || profile.lastMessageSentAt >= 2_592_000_000)) && lorittaShards.getMutualGuilds(user).size >= 10) {
			val locale = loritta.getLocaleById("default")
			val builder = EmbedBuilder()
					.setTitle("<:blobcatgooglypolice:525643372317114383> Usuário suspeito")
					.setAuthor(user.name + "#" + user.discriminator)
					.setThumbnail(user.effectiveAvatarUrl)
					.setFooter("ID do usuário: ${user.id}", null)

			if (profile != null) {
				if (profile.lastMessageSentAt != 0L) {
					val lastSeenDiff = DateUtils.formatDateDiff(profile.lastMessageSentAt, locale)
					builder.addField("\uD83D\uDC40 ${locale["USERINFO_LAST_SEEN"]}", lastSeenDiff, true)
				}
			} else {
				builder.addField("\uD83D\uDC40 ${locale["USERINFO_LAST_SEEN"]}", "**Nunca criei um perfil para esse meliante**", true)
			}

			var sharedServersFieldTitle = locale.format { commands.discord.userInfo.sharedServers }
			var servers: String?
			val sharedServers = lorittaShards.getMutualGuilds(user)
					.sortedByDescending { it.members.size }

			servers = sharedServers.joinToString(separator = ", ", transform = { "`${it.name}` *${DateUtils.formatDateDiff(it.getMember(user).joinDate.toInstant().toEpochMilli(), locale)}*" })
			sharedServersFieldTitle = "$sharedServersFieldTitle (${sharedServers.size})"

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "..."
			}

			builder.addField("\uD83C\uDF0E $sharedServersFieldTitle", servers, true)

			channel.sendMessage(builder.build()).queue()
		}
	}
}