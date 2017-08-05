package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AtaCommand : CommandBase() {
	override fun getLabel(): String {
		return "ata"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("ATA_DESCRIPTION")
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

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.FOLDER + "ata.png")); // Template
		var base = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)
		var scaled = contextImage.getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		var transformed = LorittaImage(scaled)
		transformed.setCorners(107F, 0F,
				300F, 0F,
				300F, 177F,
				96F, 138F)

		base.graphics.drawImage(transformed.bufferedImage, 0, 0, null);
		base.graphics.drawImage(template, 0, 0, null)

		context.sendFile(base, "ata.png", context.getAsMention(true));
	}
}