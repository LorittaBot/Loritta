package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.TimeUnit

class AFKModule : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		return (event.channel as TextChannel).canTalk()
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		val afkMembers = mutableListOf<Pair<Member, String?>>()

		for (mention in event.message.mentionedMembers) {
			val lorittaProfile = loritta.getLorittaProfileAsync(mention.user.idLong)

			if (lorittaProfile != null && lorittaProfile.isAfk) {
				var reason = lorittaProfile.afkReason

				if (reason != null) {
					if (MiscUtils.hasInvite(reason.stripZeroWidthSpace())) {
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
								message = locale.toNewLocale()["loritta.modules.afk.userIsAfk", "**" + afkMembers[0].first.effectiveName.escapeMentions().stripCodeMarks() + "**"] + if (afkMembers[0].second != null) {
									" **" + locale.toNewLocale()["commands.moderation.punishmentReason"] + "** » `${afkMembers[0].second}`"
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
								message = locale.toNewLocale()["loritta.modules.afk.usersAreAfk", afkMembers.joinToString(separator = ", ", transform = { "**" + it.first.effectiveName.escapeMentions().stripCodeMarks() + "**" })],
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