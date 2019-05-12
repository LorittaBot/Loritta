package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class OCRCommand : AbstractCommand("ocr", listOf("ler", "read"), CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["OCR_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		ByteArrayOutputStream().use {
			ImageIO.write(contextImage, "png", it)
			val json = """{"requests":[{"features":[{"maxResults":1,"type":"TEXT_DETECTION"}],"image":{"content":"${Base64.getEncoder().encodeToString(it.toByteArray())}"}}]}"""
			val response = HttpRequest.post("https://content-vision.googleapis.com/v1/images:annotate?key=${loritta.config.googleVision.apiKey}&alt=json")
					.contentType("application/json")
					.header("Content-Length", json.toByteArray().size)
					.header("Content-Type", "application/json")
					.userAgent("Google-API-Java-Client Google-HTTP-Java-Client/1.21.0 (gzip)")
					.send(json)
			val body = response.body()

			val parsedResponse = jsonParser.parse(body)

			val builder = EmbedBuilder()
			builder.setTitle("\uD83D\uDCDD\uD83D\uDD0D OCR")
			try {
				builder.setDescription("```${parsedResponse["responses"][0]["textAnnotations"][0]["description"].string}```")
			} catch (e: Exception) {
				builder.setDescription("**${locale["OCR_COUDLNT_FIND"]}**")
			}
			context.sendMessage(context.getAsMention(true), builder.build())
		}
	}
}