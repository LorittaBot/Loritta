package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.gifs.CepoDeMadeiraGIF
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class CepoCommand : CommandBase("cepo") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.CEPO_DESCRIPTION.f();
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var file = CepoDeMadeiraGIF.getGIF(contextImage);

		context.sendFile(file, "cepo.gif", context.getAsMention(true));
		file.delete()
	}
}