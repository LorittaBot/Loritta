package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.gifs.CepoDeMadeiraGIF

class CepoCommand : CommandBase() {
	override fun getLabel(): String {
		return "cepo"
	}

	override fun getDescription(): String {
		return "Destrua alguém no estilo Cepo de Madeira!";
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
		var file = CepoDeMadeiraGIF.getGIF(contextImage);

		context.sendFile(file, "cepo.gif", context.getAsMention(true));
	}
}