package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.awt.Color

class InverterCommand : AbstractCommand("invert", listOf("inverter"), category = CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.invert.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Detailed Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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