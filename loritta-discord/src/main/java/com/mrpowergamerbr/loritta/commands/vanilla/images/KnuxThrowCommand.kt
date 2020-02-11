package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.gifs.KnucklesThrowGIF
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class KnuxThrowCommand : AbstractCommand("knuxthrow", listOf("knucklesthrow", "throwknux", "throwknuckles", "knucklesjogar", "knuxjogar", "jogarknuckles", "jogarknux"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["KNUXTHROW_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta")
	}

	override fun getUsage(): String {
		return "<imagem>"
	}

	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val file = KnucklesThrowGIF.getGIF(contextImage)

		MiscUtils.optimizeGIF(file, 50)
		context.sendFile(file, "knuxthrow.gif", context.getAsMention(true))
		file.delete()
	}
}