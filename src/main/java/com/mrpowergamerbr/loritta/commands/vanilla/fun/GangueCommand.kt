package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class GangueCommand : CommandBase() {
	override fun getLabel(): String {
		return "gangue"
	}

	override fun getDescription(): String {
		return "Gangue da quebrada";
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta @MrPowerGamerBR @Best Player @Giovanna_GGold @Nirewen");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<usuário 1> <usuário 2> <usuário 3> <usuário 4> <usuário 5>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
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
		val template = ImageIO.read(File(Loritta.FOLDER + "cocielo/cocielo.png")); // Template
		val overlay = ImageIO.read(File(Loritta.FOLDER + "cocielo/overlay.png")); // Overlay

		val scaled = contextImage.getScaledInstance(236, 236, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(80)
		val scaled2 = contextImage2.getScaledInstance(191, 230, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(80)
		val scaled3 = contextImage3.getScaledInstance(202, 202, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(80)
		val scaled4 = contextImage4.getScaledInstance(213, 233, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(80)
		val scaled5 = contextImage5.getScaledInstance(174, 174, BufferedImage.SCALE_SMOOTH)
				.toBufferedImage()
				.makeRoundedCorners(80)

		// Porque nós precisamos rotacionar
		val rotated = javaxt.io.Image(scaled5)
		rotated.rotate(335.0);

		template.graphics.drawImage(scaled, 867, 322, null);
		template.graphics.drawImage(scaled2, 571, 349, null);
		template.graphics.drawImage(scaled3, 1381, 320, null);
		template.graphics.drawImage(scaled4, 112, 565, null);
		template.graphics.drawImage(rotated.bufferedImage, 1160, -20, null);
		template.graphics.drawImage(overlay, 0, 0, null);
		context.sendFile(template, "gangue.png", context.getAsMention(true));
	}
}