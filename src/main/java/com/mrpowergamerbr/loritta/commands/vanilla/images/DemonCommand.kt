package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.gifs.DemonGIF
import com.mrpowergamerbr.loritta.utils.gifs.MentionGIF
import com.mrpowergamerbr.loritta.utils.gifs.SwingGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class DemonCommand : AbstractCommand("demon", listOf("demônio", "demonio", "demónio"), category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DEMON_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta @SparklyBot");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0)
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var file = DemonGIF.getGIF(contextImage, context.config.guildId)

		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "demon.gif", context.getAsMention(true))
		file.delete()
	}
}