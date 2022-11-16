package net.perfectdreams.loritta.morenitta.modules

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.utils.escapeMentions
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.utils.stripZeroWidthSpace
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import java.util.concurrent.TimeUnit

class AFKModule(val loritta: LorittaBot) : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return event.textChannel?.canTalk() == true
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val afkMembers = mutableListOf<Pair<Member, String?>>()

		for (mention in event.message.mentions.members) {
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
						LorittaReply(
                                message = locale["loritta.modules.afk.userIsAfk", "**" + afkMembers[0].first.effectiveName.escapeMentions().stripCodeMarks() + "**"] + if (afkMembers[0].second != null) {
                                    " **" + locale["commands.category.moderation.punishmentReason"] + "** » `${afkMembers[0].second}`"
                                } else {
                                    ""
                                },
                                prefix = "\uD83D\uDE34"
                        ).build(JDAUser(event.author))
				).queue {
					it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
				}
			} else {
				val replies = mutableListOf<LorittaReply>()
				replies.add(
                        LorittaReply(
                                message = locale["loritta.modules.afk.usersAreAfk", afkMembers.joinToString(separator = ", ", transform = { "**" + it.first.effectiveName.escapeMentions().stripCodeMarks() + "**" })],
                                prefix = "\uD83D\uDE34"
                        )
				)
				for ((member, reason) in afkMembers.filter { it.second != null }) {
					replies.add(
                            LorittaReply(
                                    message = "**" + member.effectiveName.escapeMentions().stripCodeMarks() + "** » `${reason!!.stripCodeMarks().replace("discord.gg", "")}`",
                                    mentionUser = false
                            )
					)
				}
				event.channel.sendMessage(
						replies.joinToString("\n") { it.build(JDAUser(event.author)) }
				).queue {
					it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
				}
			}
		}

		return false
	}
}