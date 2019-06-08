package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import java.awt.Image
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class DrakeCommand : AbstractCommand("drake", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["DRAKE_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("", "@Loritta @MrPowerGamerBR")
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {}
			argument(ArgumentType.USER) {}
		}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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