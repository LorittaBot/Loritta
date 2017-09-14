package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.awt.Color
import java.io.StringReader
import java.net.URLEncoder

class WikiaCommand : CommandBase() {
	override fun getLabel(): String {
		return "wikia"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("WIKIA_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "url conteúdo"
	}

	override fun getExample(): List<String> {
		return listOf("parappatherapper Katy Kat", "dbz Goku", "undertale Asriel Dreemurr")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("url" to "URL de uma Wikia, se a URL de uma Wikia é \"http://naruto.wikia.com\", você deverá colocar \"naruto\"",
				"conteúdo" to "O que você deseja procurar na Wikia")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 2) {
			val websiteId = context.args[0]

			val query = StringUtils.join(context.args, " ", 1, context.args.size)
			val body = HttpRequest.get("http://" + websiteId + ".wikia.com/api/v1/Search/List/?query=" + URLEncoder.encode(query, "UTF-8") + "&limit=1&namespaces=0%2C14").body()

			// Resolvi usar JsonParser em vez de criar um objeto para o Gson desparsear..
			val reader = StringReader(body)
			val jsonReader = JsonReader(reader)
			jsonReader.isLenient = true
			try {
				val wikiaResponse = JsonParser().parse(jsonReader).asJsonObject // Base

				if (wikiaResponse.has("exception")) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.WIKIA_COULDNT_FIND.msgFormat(query, websiteId))
				} else {
					val item = wikiaResponse.get("items").asJsonArray.get(0).asJsonObject // Nós iremos pegar o 0, já que é o primeiro resultado

					val pageTitle = item.get("title").asString
					val pageExtract = Jsoup.parse(item.get("snippet").asString).text()
					val pageUrl = item.get("url").asString

					val embed = EmbedBuilder()
							.setTitle(pageTitle, pageUrl)
							.setColor(Color.BLUE)
							.setDescription(if (pageExtract.length > 2048) pageExtract.substring(0, 2044) + "..." else pageExtract)

					context.sendMessage(embed.build()) // Envie a mensagem!
				}
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.WIKIA_COULDNT_FIND.msgFormat(query, websiteId))
			}
		} else {
			this.explain(context);
		}
	}
}