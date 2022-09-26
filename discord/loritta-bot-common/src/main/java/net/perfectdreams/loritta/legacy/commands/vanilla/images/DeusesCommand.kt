package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.ImageUtils
import net.perfectdreams.loritta.legacy.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DeusesCommand : AbstractCommand("deuses", category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "deuses.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.gods.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.gods.examples")
	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val texto = context.args.joinToString(" ")

			// Vamos criar o nosso tempalte
			val image = BufferedImage(630, 830, BufferedImage.TYPE_INT_ARGB)
			val graphics = image.graphics.enableFontAntiAliasing()
			graphics.color = Color.WHITE
			graphics.fillRect(0, 0, 630, 830)
			graphics.color = Color.BLACK
			graphics.drawImage(TEMPLATE, 0, 200, null)

			val font = Font.createFont(0, File(Loritta.ASSETS + "mavenpro-bold.ttf")).deriveFont(42F)
			graphics.font = font
			ImageUtils.drawTextWrapSpaces(texto, 2, 40, 630, 9999, graphics.fontMetrics, graphics)

			context.sendFile(image, "deuses.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}