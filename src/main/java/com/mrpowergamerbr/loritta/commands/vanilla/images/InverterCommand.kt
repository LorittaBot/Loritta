package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.util.*

class InverterCommand : AbstractCommand("invert", listOf("inverter"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["INVERTER_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return Arrays.asList("http://i.imgur.com/KbHXmKO.png", "@Loritta", "\uD83D\uDC4C")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return ImmutableMap.builder<String, String>()
				.put("mensagem", "Usuário sortudo")
				.build()
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		for (x in 0 until image.width) {
			for (y in 0 until image.height) {
				val rgba = image.getRGB(x, y)
				var col = Color(rgba, true)
				col = Color(
						255 - col.red,
						255 - col.green,
						255 - col.blue)
				image.setRGB(x, y, col.rgb)
			}
		}

		context.sendFile(image, "invertido.png", context.getAsMention(true))
	}
}