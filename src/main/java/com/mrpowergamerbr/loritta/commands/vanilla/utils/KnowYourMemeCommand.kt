package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.URLEncoder

class KnowYourMemeCommand : CommandBase() {
	override fun getLabel(): String {
		return "knowyourmeme"
	}

	override fun getAliases(): List<String> {
		return listOf("kym")
	}

	override fun getDescription(): String {
		return "Procura um meme no KnowYourMeme"
	}

	override fun getExample(): List<String> {
		return listOf("Arthur's Headphones")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" "); // Vamos juntar a nossa query

			val response = HttpRequest.get("http://rkgk.api.searchify.com/v1/indexes/kym_production/instantlinks?query=" + URLEncoder.encode(query, "UTF-8") + "&fetch=*")
					.body() // Vamos pegar a response...

			// E vamos parsear!
			val json = JsonParser().parse(response).obj

			if (json["matches"].int == 0) {
				// Nada foi encontrado...
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Não encontrei nada relacionado a `$query` no KnowYourMeme...")
				return;
			} else {
				// Algo foi encontrado!
				val meme = json["results"][0]
				val name = meme["name"].string
				val origin = meme["origin"].string
				val iconUrl = meme["icon_url"].string
				val originDate = meme["origin_date"].string
				val summary = meme["summary"].string
				val url = meme["url"].string

				val embed = EmbedBuilder()

				embed.setTitle("<:kym:331052564357578754> $name", "https://knowyourmeme.com$url")
				embed.setThumbnail(iconUrl)
				embed.setDescription(summary)
				embed.addField("\uD83C\uDF1F Origem", if (origin.isNotBlank()) origin else "Desconhecido", true)
				embed.addField("\uD83D\uDCC6 Data", if (originDate.isNotBlank()) originDate else "Desconhecido", true)
				embed.setColor(Color(18, 19, 63))

				context.sendMessage(embed.build())
			}
		} else {
			context.explain()
		}
	}
}