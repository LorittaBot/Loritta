package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.utils.ImageUtils
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.api.commands.Command
import net.perfectdreams.loritta.legacy.api.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.legacy.api.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.ImageAbstractCommandBase
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File

class MorrePragaCommand(m: LorittaDiscord) : ImageAbstractCommandBase(m, listOf("dieplague", "morrepraga")) {
	override fun command() = create {
		localizedDescription("commands.command.morrepraga.description")
		localizedExamples(Command.SINGLE_IMAGE_EXAMPLES_KEY)

		usage {
			argument(ArgumentType.IMAGE) {}
		}

		needsToUploadFiles = true

		executes {
			// TODO: Multiplatform
			val mppImage = validate(image(0))
			mppImage as JVMImage
			val mppMorrePraga = loritta.assets.loadImage("morre_praga.png", loadFromCache = false)
			val template = (mppMorrePraga as JVMImage).handle as BufferedImage

			val contextImage = mppImage.handle as BufferedImage
			val graphics = template.createGraphics()

			val scaled = contextImage.getScaledInstance(312, 312, BufferedImage.SCALE_SMOOTH)
			
			graphics.enableFontAntiAliasing()
			val lato = Font.createFont(Font.TRUETYPE_FONT, File(Loritta.ASSETS, "fonts/Cambria-Italic.ttf"))
			val font = lato.deriveFont(150f)
			graphics.color = Color.BLACK
			graphics.font = font

			ImageUtils.drawCenteredString(
				graphics,
				locale["commands.command.morrepraga.topText"],
				Rectangle(25, 38, 502, 132),
				graphics.font
			)

			ImageUtils.drawCenteredString(
				graphics,
				locale["commands.command.morrepraga.bottomText"],
				Rectangle(43, 480, 468, 139),
				graphics.font
			)
			
			graphics.drawImage(scaled, 115, 183, null)

			sendImage(JVMImage(template), "morre_praga.png")
		}
	}
}