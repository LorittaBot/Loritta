package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.gifs.DemonGIF
import com.mrpowergamerbr.loritta.utils.gifs.KnucklesThrowGIF
import com.mrpowergamerbr.loritta.utils.gifs.MentionGIF
import com.mrpowergamerbr.loritta.utils.gifs.SwingGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class KnuxThrowCommand : AbstractCommand("knuxthrow", listOf("knucklesthrow", "throwknux", "throwknuckles", "knucklesjogar", "knuxjogar", "jogarknuckles", "jogarknux"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["KNUXTHROW_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles() = true

	override fun run(context: CommandContext, locale: BaseLocale) {
		val contextImage = LorittaUtils.getImageFromContext(context, 0)
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return
		}
		val file = KnucklesThrowGIF.getGIF(contextImage)

		MiscUtils.optimizeGIF(file, 50)
		context.sendFile(file, "knuxthrow.gif", context.getAsMention(true))
		file.delete()
	}
}