package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils


class AminoCommand : CommandBase() {
	override fun getLabel(): String {
		return "amino"
	}

	override fun getUsage(): String {
		return "<arquivo do Amino>"
	}

	override fun getDescription(): String {
		return "Carrega e envia uma imagem do Amino (arquivos com extensão \".Amino\")"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		var image = LorittaUtils.getImageFromContext(context, 0);

		if (!LorittaUtils.isValidImage(context, image)) {
			return;
		}

		// Hora de enviar a imagem!
		context.sendFile(image, "amino.png", context.getAsMention(true));
	}
}