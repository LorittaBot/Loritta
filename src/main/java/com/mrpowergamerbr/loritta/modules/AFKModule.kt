package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import java.util.concurrent.TimeUnit

class AFKModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return (event.channel as TextChannel).canTalk()
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val afkMembers = mutableListOf<Pair<Member, String?>>()

		for (mention in event.message.mentionedMembers) {
			val lorittaProfile = loritta.getLorittaProfileForUser(mention.user.id)

			if (lorittaProfile.isAfk) {
				var reason = lorittaProfile.afkReason

				if (reason != null) {
					val matcher = Constants.URL_PATTERN.matcher(reason
							.replace("\u200B", "")
							.replace("\\", ""))

					while (matcher.find()) {
						var url = matcher.group()
						if (url.contains("discord") && url.contains("gg")) {
							url = "discord.gg" + matcher.group(1).replace(".", "")
						}
						val inviteId = MiscUtils.getInviteId("http://$url") ?: MiscUtils.getInviteId("https://$url") ?: break

						reason = "¯\\_(ツ)_/¯"
					}
				}
				afkMembers.add(Pair(mention, reason))
			}
		}

		if (afkMembers.isNotEmpty()) {
			if (afkMembers.size == 1) {
				event.channel.sendMessage(
						LoriReply(
								message = locale["AFK_UserIsAfk", "**" + afkMembers[0].first.effectiveName.escapeMentions().stripCodeMarks() + "**"] + if (afkMembers[0].second != null) {
									" **" + locale["HACKBAN_REASON"] + "** » `${afkMembers[0].second}`"
								} else {
									""
								},
								prefix = "\uD83D\uDE34"
						).build(event.author)
				).queue {
					it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
				}
			} else {
				val replies = mutableListOf<LoriReply>()
				replies.add(
						LoriReply(
								message = locale["AFK_UsersAreAFK", afkMembers.joinToString(separator = ", ", transform = { "**" + it.first.effectiveName.escapeMentions().stripCodeMarks() + "**" })],
								prefix = "\uD83D\uDE34"
						)
				)
				for ((member, reason) in afkMembers.filter { it.second != null }) {
					replies.add(
							LoriReply(
									message = "**" + member.effectiveName.escapeMentions().stripCodeMarks() + "** » `${reason!!.stripCodeMarks().replace("discord.gg", "")}`",
									mentionUser = false
							)
					)
				}
				event.channel.sendMessage(
						replies.map { it.build(event.author) }.joinToString("\n")
				).queue {
					it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
				}
			}
		}

		return false
	}
}