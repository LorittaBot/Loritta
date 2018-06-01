package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.gifs.MentionGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class DiscordiaCommand : AbstractCommand("mentions", listOf("discórdia", "discord", "discordia"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DISCORDIA_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun needsToUploadFiles() = true

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var file = MentionGIF.getGIF(contextImage);
		MiscUtils.optimizeGIF(file)
		context.sendFile(file, "discordia.gif", context.getAsMention(true));
		file.delete()
	}
}