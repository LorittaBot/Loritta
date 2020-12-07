package net.perfectdreams.loritta.commands.vanilla.utils

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.gson
import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class OCRCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("ocr", "ler", "read"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.ocr"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		executesDiscord {
			val context = this

			val contextImage = context.imageOrFail(0)

			val image = (contextImage as JVMImage).handle

			ByteArrayOutputStream().use {
				ImageIO.write(image as BufferedImage,"png", it)

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

				val response = com.mrpowergamerbr.loritta.utils.loritta.http.post<io.ktor.client.statement.HttpResponse>("https://content-vision.googleapis.com/v1/images:annotate?key=${com.mrpowergamerbr.loritta.utils.loritta.config.googleVision.apiKey}&alt=json") {
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
					builder.setDescription("**${locale["$LOCALE_PREFIX.couldntFind"]}**")
				}
				context.sendMessage(context.getUserMention(true), builder.build())
			}
		}
	}
}