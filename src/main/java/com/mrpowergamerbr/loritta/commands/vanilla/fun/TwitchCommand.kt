package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class TwitchCommand : CommandBase("twitch") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["TWITCH_Description"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("mrpowergamerbr", "velberan", "coredasantigas")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var query = context.args.joinToString(" ");

			val payload = HttpRequest.get("https://api.twitch.tv/helix/users?login=${query}")
					.header("Client-ID", Loritta.config.twitchClientId)
					.body()

			val response = JSON_PARSER.parse(payload).obj

			val data = response["data"].array

			if (data.size() == 0) {
				context.reply(
						LoriReply(
								context.locale["YOUTUBE_COULDNT_FIND", query],
								Constants.ERROR
						)
				)
				return
			}

			val channel = data[0].obj
			val channelName = channel["display_name"].string
			val isPartner = channel["broadcaster_type"].string == "partner"
			val description = channel["description"].string
			val avatarUrl = channel["profile_image_url"].string
			val offlineImageUrl = channel["offline_image_url"].string
			val viewCount = channel["view_count"].long

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
				addField("\uD83D\uDCFA ${context.locale["MUSICINFO_VIEWS"]}", viewCount.toString(), true)
			}

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			context.explain()
		}
	}
}