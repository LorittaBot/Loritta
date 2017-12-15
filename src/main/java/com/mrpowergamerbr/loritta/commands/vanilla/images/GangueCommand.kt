package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class GangueCommand : AbstractCommand("gangue", listOf("gang")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.GANGUE_DESCRIPTION.f()
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta @MrPowerGamerBR @Best Player @Giovanna_GGold @Nirewen");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun getUsage(): String {
		return "<usuário 1> <usuário 2> <usuário 3> <usuário 4> <usuário 5>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		val contextImage2 = LorittaUtils.getImageFromContext(context, 1);
		if (!LorittaUtils.isValidImage(context, contextImage2)) {
			return;
		}
		val contextImage3 = LorittaUtils.getImageFromContext(context, 2);
		if (!LorittaUtils.isValidImage(context, contextImage3)) {
			return;
		}
		val contextImage4 = LorittaUtils.getImageFromContext(context, 3);
		if (!LorittaUtils.isValidImage(context, contextImage4)) {
			return;
		}
		val contextImage5 = LorittaUtils.getImageFromContext(context, 4);
		if (!LorittaUtils.isValidImage(context, contextImage5)) {
			return;
		}
		val template = ImageIO.read(File(Loritta.ASSETS + "cocielo/cocielo.png")); // Template
		val overlay = ImageIO.read(File(Loritta.ASSETS + "cocielo/overlay.png")); // Overlay

		val scaled = contextImage.getScaledInstance(59, 59, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled2 = contextImage2.getScaledInstance(47, 57, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled3 = contextImage3.getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled4 = contextImage4.getScaledInstance(53, 58, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)
		val scaled5 = contextImage5.getScaledInstance(43, 43, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(20)

		// Porque nós precisamos rotacionar
		val rotated = LorittaImage(scaled5)
		rotated.rotate(335.0);

		template.graphics.drawImage(scaled, 216, 80, null);
		template.graphics.drawImage(scaled2, 142, 87, null);
		template.graphics.drawImage(scaled3, 345, 80, null);
		template.graphics.drawImage(scaled4, 28, 141, null);
		template.graphics.drawImage(rotated.bufferedImage, 290, -5, null);
		template.graphics.drawImage(overlay, 0, 0, null);
		context.sendFile(template, "gangue.png", context.getAsMention(true));
	}
}