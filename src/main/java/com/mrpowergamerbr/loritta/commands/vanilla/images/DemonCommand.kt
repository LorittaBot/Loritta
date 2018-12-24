package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.gifs.DemonGIF
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class DemonCommand : AbstractCommand("demon", listOf("demônio", "demonio", "demónio"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DEMON_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta @SparklyBot")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val file = DemonGIF.getGIF(contextImage, context.config.guildId)

		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "demon.gif", context.getAsMention(true))
		file.delete()
	}
}