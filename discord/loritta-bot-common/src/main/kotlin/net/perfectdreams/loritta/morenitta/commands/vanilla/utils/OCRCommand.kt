package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import net.perfectdreams.loritta.morenitta.LorittaBot

class OCRCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ocr", listOf("ler", "read"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
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

			val responses = loritta.googleVisionOCRClient.ocr(it.toByteArray())

			val builder = EmbedBuilder()
			builder.setTitle("\uD83D\uDCDD\uD83D\uDD0D OCR")
			try {
				builder.setDescription("```${responses.responses.first().textAnnotations!!.first().description}```")
			} catch (e: Exception) {
				builder.setDescription("**${locale["commands.command.ocr.couldntFind"]}**")
			}
			context.sendMessage(context.getAsMention(true), builder.build())
		}
	}
}