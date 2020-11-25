package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class OCRCommand : AbstractCommand("ocr", listOf("ler", "read"), CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.utils.ocr.description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		ByteArrayOutputStream().use {
			ImageIO.write(contextImage, "png", it)

			val json = jsonObject(
					"requests" to jsonArray(
							jsonObject(
									"features" to jsonArray(
											jsonObject(
													"maxResults" to 1,
													"type" to "TEXT_DETECTION"
											)
									),
									"image" to jsonObject(
											"content" to Base64.getEncoder().encodeToString(it.toByteArray())
									)

							)
					)
			)

			val response = loritta.http.post<io.ktor.client.statement.HttpResponse>("https://content-vision.googleapis.com/v1/images:annotate?key=${loritta.config.googleVision.apiKey}&alt=json") {
				contentType(ContentType.Application.Json)
				userAgent("Google-API-Java-Client Google-HTTP-Java-Client/1.21.0 (gzip)")

				body = gson.toJson(json)
			}

			val body = response.receive<String>()

			val parsedResponse = JsonParser.parseString(body)

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