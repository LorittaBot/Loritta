package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PerfeitoCommand : CommandBase("perfeito") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.PERFEITO_DESCRIPTION.f();
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

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0, 25, 256);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.ASSETS + "perfeito.png")); // Template

		var scaled = contextImage.getScaledInstance(231, 231, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 225, 85, null);

		context.sendFile(template, "perfeito.png", context.getAsMention(true));
	}
}