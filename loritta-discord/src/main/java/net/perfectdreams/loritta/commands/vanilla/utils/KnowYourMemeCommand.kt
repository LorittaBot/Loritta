package net.perfectdreams.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import java.awt.Color
import java.net.URLEncoder

class KnowYourMemeCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("knowyourmeme", "kym"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.knowyourmeme"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.TEXT) {}
		}

		examples {
			"Arthur's headphones"
		}

		executesDiscord {
			val context = this

			if (context.args.isNotEmpty()) {
				val query = context.args.joinToString(" ")

				val response = HttpRequest.get("http://rkgk.api.searchify.com/v1/indexes/kym_production/instantlinks?query=" + URLEncoder.encode(query, "UTF-8") + "&fetch=*")
						.body()

				val json = JsonParser.parseString(response).obj

				if (json["matches"].int == 0) {
					context.reply(
							LorittaReply(
									locale["$LOCALE_PREFIX.couldntFind", query],
									Constants.ERROR
							)
					)
					return@executesDiscord
				} else {
					val meme = json["results"][0]
					val name = meme["name"].string
					val origin = meme["origin"].string
					val iconUrl = meme["icon_url"].string
					val originDate = meme["origin_date"].string
					val summary = if (meme.obj.has("summary")) {
						meme["summary"].string
					} else {
						context.locale["$LOCALE_PREFIX.noDescription"]
					}
					val url = meme["url"].string

					val embed = EmbedBuilder()

					embed.setTitle("<:kym:375313574085787648> $name", "http://knowyourmeme.com$url")
					embed.setThumbnail(iconUrl)
					embed.setDescription(summary)
					embed.addField("\uD83C\uDF1F ${locale["$LOCALE_PREFIX.origin"]}", if (origin.isNotBlank()) origin else context.locale["$LOCALE_PREFIX.unknown"], true)
					embed.addField("\uD83D\uDCC6 ${locale["$LOCALE_PREFIX.date"]}", if (originDate.isNotBlank()) originDate else context.locale["$LOCALE_PREFIX.unknown"], true)
					embed.setColor(Color(18, 19, 63))

					context.sendMessage(embed.build())
				}
			} else {
				context.explain()
			}
		}
	}
}