package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color
import java.net.URLEncoder

class KnowYourMemeCommand : AbstractCommand("knowyourmeme", listOf("kym"), CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.utils.knowyourmeme.description"]
	}

	override fun getExamples(): List<String> {
		return listOf("Arthur's Headphones")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ") // Vamos juntar a nossa query

			val response = HttpRequest.get("http://rkgk.api.searchify.com/v1/indexes/kym_production/instantlinks?query=" + URLEncoder.encode(query, "UTF-8") + "&fetch=*")
					.body() // Vamos pegar a response...

			// E vamos parsear!
			val json = JsonParser.parseString(response).obj

			if (json["matches"].int == 0) {
				// Nada foi encontrado...
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.utils.knowyourmeme.couldntFind", query])
				return
			} else {
				// Algo foi encontrado!
				val meme = json["results"][0]
				val name = meme["name"].string
				val origin = meme["origin"].string
				val iconUrl = meme["icon_url"].string
				val originDate = meme["origin_date"].string
				val summary = if (meme.obj.has("summary")) {
					meme["summary"].string
				} else {
					context.locale["commands.utils.knowyourmeme.noDescription"]
				}
				val url = meme["url"].string

				val embed = EmbedBuilder()

				embed.setTitle("<:kym:375313574085787648> $name", "http://knowyourmeme.com$url")
				embed.setThumbnail(iconUrl)
				embed.setDescription(summary)
				embed.addField("\uD83C\uDF1F ${locale.toNewLocale()["commands.utils.knowyourmeme.origin"]}", if (origin.isNotBlank()) origin else context.locale["commands.utils.knowyourmeme.unknown"], true)
				embed.addField("\uD83D\uDCC6 ${locale.toNewLocale()["commands.utils.knowyourmeme.date"]}", if (originDate.isNotBlank()) originDate else context.locale["commands.utils.knowyourmeme.unknown"], true)
				embed.setColor(Color(18, 19, 63))

				context.sendMessage(embed.build())
			}
		} else {
			context.explain()
		}
	}
}