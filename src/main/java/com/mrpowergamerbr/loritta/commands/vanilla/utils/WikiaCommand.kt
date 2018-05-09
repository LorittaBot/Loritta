package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.net.URLEncoder

class WikiaCommand : AbstractCommand("wikia", category = CommandCategory.UTILS) {
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

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size >= 2) {
			val websiteId = context.args[0]

			val wikiaUrl = "http://$websiteId.wikia.com"
			val metadataResponse = HttpRequest.get("$wikiaUrl/api/v1/Mercury/WikiVariables")
			val metadataBody = metadataResponse.body()

			if (!MiscUtils.isJSONValid(metadataBody)) {
				context.reply(
						LoriReply("Wikia `$websiteId` não existe!", Constants.ERROR)
				)
				return
			}

			val metadata = jsonParser.parse(metadataBody)["data"].obj
			val wikiaImage = if (metadata.has("image") && !metadata["image"].isJsonNull) {
				metadata["image"].string.split("/revision")[0]
			} else {
				"https://slot1-images.wikia.nocookie.net/__cb1508839704/common/skins/common/images/wiki.png"
			}
			val wikiaName = metadata["siteName"].string

			val query = StringUtils.join(context.args, " ", 1, context.args.size)
			val body = try {
				HttpRequest.get("$wikiaUrl/api/v1/Search/List/?query=" + URLEncoder.encode(query, "UTF-8") + "&limit=1&namespaces=0%2C14").body()
			} catch (e: Exception) {
				null
			}

			if (body == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["WIKIA_COULDNT_FIND", query, websiteId])
				return
			}

			// Resolvi usar JsonParser em vez de criar um objeto para o Gson desparsear..
			try {
				val wikiaResponse = jsonParser.parse(body).obj // Base

				if (wikiaResponse.has("exception")) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["WIKIA_COULDNT_FIND", query, websiteId])
				} else {
					val item = wikiaResponse.get("items").array[0].obj // Nós iremos pegar o 0, já que é o primeiro resultado

					val response = HttpRequest.get("$wikiaUrl/api/v1/Articles/AsSimpleJson?id=${item["id"].string}")
							.body()

					val json = jsonParser.parse(response).obj

					val sections = json["sections"].array
					var image: String? = null

					for (_section in sections) {
						val section = _section.obj
						if (image == null && section.has("images")) {
							val images = section["images"].array
							if (images.size() > 0) {
								image = images[0]["src"].string
							}
						}
					}

					val pageTitle = item.get("title").asString
					val pageUrl = item.get("url").asString

					val embed = EmbedBuilder().apply {
						setTitle("<:fandom:372531714502819852> $pageTitle", pageUrl)
						setAuthor(wikiaName, wikiaUrl, wikiaImage)
						setColor(Color(57, 233, 0))
						setThumbnail(image)

						for (_section in sections) {
							val section = _section.obj
							var sectionText = ""
							val contents = section["content"].array
							for (content in contents) {
								if (content.obj.has("text")) {
									val contentText = content["text"].string
									sectionText += "$contentText\n"
								}
							}
							setDescription(sectionText.substringIfNeeded(0 until 2048))
							break
						}
					}

					context.sendMessage(context.getAsMention(true), embed.build()) // Envie a mensagem!
				}
			} catch (e: Exception) {
				e.printStackTrace()
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["WIKIA_COULDNT_FIND", query, websiteId])
			}
		} else {
			this.explain(context);
		}
	}
}