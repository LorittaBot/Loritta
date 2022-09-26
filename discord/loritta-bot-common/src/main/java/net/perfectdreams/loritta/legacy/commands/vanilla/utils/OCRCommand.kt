package net.perfectdreams.loritta.legacy.commands.vanilla.utils

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.utils.loritta
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class OCRCommand : AbstractCommand("ocr", listOf("ler", "read"), CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.ocr.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0, createTextAsImageIfNotFound = false) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

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

			val response = loritta.http.post("https://content-vision.googleapis.com/v1/images:annotate?key=${loritta.config.googleVision.apiKey}&alt=json") {
				contentType(ContentType.Application.Json)
				userAgent("Google-API-Java-Client Google-HTTP-Java-Client/1.21.0 (gzip)")

				setBody(gson.toJson(json))
			}

			val body = response.bodyAsText()

			val parsedResponse = JsonParser.parseString(body)

			val builder = EmbedBuilder()
			builder.setTitle("\uD83D\uDCDD\uD83D\uDD0D OCR")
			try {
				builder.setDescription("```${parsedResponse["responses"][0]["textAnnotations"][0]["description"].string}```")
			} catch (e: Exception) {
				builder.setDescription("**${locale["commands.command.ocr.couldntFind"]}**")
			}
			context.sendMessage(context.getAsMention(true), builder.build())
		}
	}
}