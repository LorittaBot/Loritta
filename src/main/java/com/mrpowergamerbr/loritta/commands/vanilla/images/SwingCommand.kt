package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.gifs.SwingGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class SwingCommand : AbstractCommand("swing", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SWING_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta @SparklyBot");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles() = true

	override fun run(context: CommandContext, locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val contextImage2 = context.getImageAt(1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		val file = SwingGIF.getGIF(contextImage, contextImage2)
		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "swing.gif", context.getAsMention(true))
		file.delete()
	}
}