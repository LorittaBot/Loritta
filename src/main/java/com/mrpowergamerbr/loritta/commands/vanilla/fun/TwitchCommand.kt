package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color
import java.util.*

class TwitchCommand : AbstractCommand("twitch", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["TWITCH_Description"]
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("mrpowergamerbr", "velberan", "coredasantigas")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			var query = context.args.joinToString(" ")

			if (!Constants.TWITCH_USERNAME_PATTERN.matcher(query).matches()) {
				context.reply(
						LoriReply(
								context.legacyLocale["YOUTUBE_COULDNT_FIND", query],
								Constants.ERROR
						)
				)
				return
			}

			val payload = loritta.twitch.getUserLogin(query)

			if (payload == null) {
				context.reply(
						LoriReply(
								context.legacyLocale["YOUTUBE_COULDNT_FIND", query],
								Constants.ERROR
						)
				)
				return
			}

			val channelName = payload.displayName
			val isPartner = payload.broadcasterType == "partner"
			val description = payload.description
			val avatarUrl = payload.profileImageUrl
			val offlineImageUrl = payload.offlineImageUrl
			val viewCount = payload.viewCount

			val embed = EmbedBuilder().apply {
				setColor(Color(101, 68, 154))
				setTitle("<:twitch:314349922755411970> $channelName")
				setDescription(description)
				if (avatarUrl.isNotEmpty()) {
					setThumbnail(avatarUrl)
				}
				if (offlineImageUrl.isNotEmpty()) {
					setImage(offlineImageUrl)
				}
				addField("\uD83D\uDCFA ${context.legacyLocale["MUSICINFO_VIEWS"]}", viewCount.toString(), true)
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			context.explain()
		}
	}
}