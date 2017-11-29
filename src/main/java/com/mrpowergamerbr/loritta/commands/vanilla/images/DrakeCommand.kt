package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Image
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class DrakeCommand : CommandBase("drake") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.DRAKE_DESCRIPTION.f()
	}

	override fun getExample(): List<String> {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return ImmutableMap.builder<String, String>()
				.put("usuário1", "*(Opcional)* Usuário sortudo")
				.put("usuário2", "*(Opcional)* Usuário sortudo")
				.build()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val bi = ImageIO.read(File(Loritta.ASSETS + "drake.png")) // Primeiro iremos carregar o nosso template
		val graph = bi.graphics

		run {
			val avatarImg = LorittaUtils.getImageFromContext(context, 0)

			if (!LorittaUtils.isValidImage(context, avatarImg)) {
				return
			}

			var image: Image = avatarImg;

			image = avatarImg.getScaledInstance(150, 150, Image.SCALE_SMOOTH)
			graph.drawImage(image, 150, 0, null)
		}

		run {
			var avatarImg = LorittaUtils.getImageFromContext(context, 1)

			if (!LorittaUtils.isValidImage(context, avatarImg)) {
				return
			}

			var image: Image = avatarImg;

			image = avatarImg.getScaledInstance(150, 150, Image.SCALE_SMOOTH)
			graph.drawImage(image, 150, 150, null)
		}

		context.sendFile(bi, "drake.png", context.getAsMention(true))
	}
}