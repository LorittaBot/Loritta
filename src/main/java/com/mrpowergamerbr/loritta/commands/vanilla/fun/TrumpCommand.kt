package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.gifs.TrumpGIF

class TrumpCommand : CommandBase() {
	override fun getLabel(): String {
		return "trump"
	}

	override fun getDescription(): String {
		return "O que será que o senhor presidente Trump está mostrando hoje?";
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<imagem>";
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var contextImage2 = LorittaUtils.getImageFromContext(context, 1);
		if (!LorittaUtils.isValidImage(context, contextImage2)) {
			return;
		}
		var file = TrumpGIF.getGIF(contextImage2, contextImage);

		context.sendFile(file, "trump.gif", context.getAsMention(true));
	}
}