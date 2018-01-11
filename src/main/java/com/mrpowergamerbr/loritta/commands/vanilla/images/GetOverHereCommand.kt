package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.gifs.GetOverHereGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class GetOverHereCommand : AbstractCommand("getoverhere", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("GETOVERHERE_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var file = GetOverHereGIF.getGIF(contextImage);

		context.sendFile(file, "getoverhere.gif", context.getAsMention(true));
		file.delete()
	}
}