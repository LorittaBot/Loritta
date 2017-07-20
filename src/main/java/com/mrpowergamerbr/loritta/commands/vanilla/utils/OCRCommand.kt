package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class OCRCommand : CommandBase() {
	override fun getLabel(): String {
		return "ocr"
	}

	override fun getAliases(): List<String> {
		return listOf("ler", "read")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.OCR_DESCRIPTION
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		val os = ByteArrayOutputStream()
		ImageIO.write(contextImage, "png", os);
		var json = """{"requests":[{"features":[{"maxResults":1,"type":"TEXT_DETECTION"}],"image":{"content":"${Base64.getEncoder().encodeToString(os.toByteArray())}"}}]}""";
		val response = HttpRequest.post("https://content-vision.googleapis.com/v1/images:annotate?key=${Loritta.config.googleVisionKey}&alt=json")
				.contentType("application/json")
				.header("Content-Length", json.toByteArray().size)
				.header("Content-Type", "application/json")
				.userAgent("Google-API-Java-Client Google-HTTP-Java-Client/1.22.0 (gzip)")
				.send(json)
		val body = response.body()

		val parsedResponse = JsonParser().parse(body)

		val builder = EmbedBuilder()
		builder.setTitle("\uD83D\uDCDD\uD83D\uDD0D OCR")
		try {
			builder.setDescription("```${parsedResponse["responses"][0]["textAnnotations"][0]["description"].string}```")
		} catch (e: Exception) {
			builder.setDescription("**${context.locale.OCR_COUDLNT_FIND}**")
		}
		context.sendMessage(context.getAsMention(true), builder.build())
	}
}