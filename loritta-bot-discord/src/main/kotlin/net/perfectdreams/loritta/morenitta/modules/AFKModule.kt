package net.perfectdreams.loritta.morenitta.modules

import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.utils.stripZeroWidthSpace
import java.util.*
import java.util.concurrent.TimeUnit

class AFKModule(val loritta: LorittaBot) : MessageReceivedModule {
	override suspend fun matches(
        event: LorittaMessageEvent,
        lorittaUser: LorittaUser,
        lorittaProfile: Profile?,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        i18nContext: I18nContext
    ): Boolean {
		return event.textChannel?.canTalk() == true
	}

	override suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean {
		val guild = event.guild ?: return false

		val mentionedMembers = event.message.mentions.members
		if (mentionedMembers.isEmpty())
			return false

		val mentionedMembersIds = mentionedMembers.map { it.idLong }

		val afkMembers = mutableListOf<Pair<Member, String?>>()

		// Bulk get all profiles
		val profiles = loritta.transaction {
			Profile.find {
				Profiles.id inList mentionedMembersIds
			}.toList()
		}

		for (afkMemberLorittaProfile in profiles) {
			if (afkMemberLorittaProfile.isAfk) {
				var reason = afkMemberLorittaProfile.afkReason

				if (reason != null) {
					if (MiscUtils.hasInvite(reason.stripZeroWidthSpace())) {
						reason = "¯\\_(ツ)_/¯"
					}
				}
				afkMembers.add(mentionedMembers.first { it.idLong == afkMemberLorittaProfile.userId } to reason)
			}
		}

		if (afkMembers.isNotEmpty()) {
			// Okay, so there are AFK members in the message!
			event.channel.sendMessage(
				MessageCreate {
					if (afkMembers.size == 1) {
						val (afkMemberId, afkReason) = afkMembers.first()

						styled(
							buildString {
								append(i18nContext.get(I18nKeysData.Modules.Afk.UserIsAfk("<@${afkMemberId}>")))
								if (afkReason != null) {
									append(" ")
									// To make things simpler, we will use empty set
									append(i18nContext.get(I18nKeysData.Modules.Afk.AfkReason(cleanUpForOutput(loritta, guild.idLong, emptySet(), afkReason))))
								}
							},
							Emotes.LoriSleeping
						)
					} else {
						styled(
							buildString {
								append(i18nContext.get(I18nKeysData.Modules.Afk.UsersAreAfk(afkMembers.joinToString { it.first.asMention })))

								for ((afkMemberId, afkReason) in afkMembers) {
									if (afkReason != null) {
										append("\n")
										append(
											i18nContext.get(
												I18nKeysData.Modules.Afk.AfkUserReason(
													"<@${afkMemberId}>",
													cleanUpForOutput(loritta, guild.idLong, emptySet(), afkReason)
												)
											)
										)
									}
								}
							},
							Emotes.LoriSleeping
						)
					}

					allowedMentionTypes = EnumSet.of(Message.MentionType.EMOJI)
				}
			).setMessageReference(event.messageId)
				.failOnInvalidReply(false)
				.queue {
					it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
				}
		}

		return false
	}
}