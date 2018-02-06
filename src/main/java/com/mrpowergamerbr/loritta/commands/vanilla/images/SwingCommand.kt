package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.gifs.MentionGIF
import com.mrpowergamerbr.loritta.utils.gifs.SwingGIF
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

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0)
		var contextImage2 = LorittaUtils.getImageFromContext(context, 1)
		if (!LorittaUtils.isValidImage(context, contextImage) || !LorittaUtils.isValidImage(context, contextImage2)) {
			return;
		}
		var file = SwingGIF.getGIF(contextImage, contextImage2);
		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "swing.gif", context.getAsMention(true))
		file.delete()
	}
}