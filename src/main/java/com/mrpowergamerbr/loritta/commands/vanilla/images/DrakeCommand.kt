package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Image
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class DrakeCommand : AbstractCommand("drake", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DRAKE_DESCRIPTION"]
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

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val bi = ImageIO.read(File(Loritta.ASSETS + "drake.png")) // Primeiro iremos carregar o nosso template
		val graph = bi.graphics

		run {
			val avatarImg = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
			val image = avatarImg.getScaledInstance(150, 150, Image.SCALE_SMOOTH)
			graph.drawImage(image, 150, 0, null)
		}

		run {
			val avatarImg = context.getImageAt(1) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
			val image = avatarImg.getScaledInstance(150, 150, Image.SCALE_SMOOTH)
			graph.drawImage(image, 150, 150, null)
		}

		context.sendFile(bi, "drake.png", context.getAsMention(true))
	}
}