package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.utils.extensions.drawStringWithOutline
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.ImageAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File

class TerminatorCommand(m: LorittaDiscord) : ImageAbstractCommandBase(
		m,
		listOf("terminator", "animeterminator", "terminatoranime")
) {
	override fun command() = create {
		localizedDescription("commands.command.terminator.description")
		localizedExamples("commands.command.terminator.examples")

		usage {
			argument(ArgumentType.TEXT) {}
		}

		needsToUploadFiles = true

		executes {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "terminatoranime")

			// TODO: Multiplatform
			loritta as Loritta

			val args = args.joinToString(" ")
			val split = args.split("|")

			if (2 > split.size) {
				explain()
				return@executes
			}

			val mppImage = validate(image(0))
			mppImage as JVMImage
			val mppTerminatorAnime = loritta.assets.loadImage("terminator_anime.png", loadFromCache = true)
			val terminatorAnime = (mppTerminatorAnime as JVMImage).handle as BufferedImage

			val input1 = split[0]
			val input2 = split[1]

			val graphics = terminatorAnime.createGraphics()

			graphics.enableFontAntiAliasing()
			val lato = Font.createFont(Font.TRUETYPE_FONT, File(Loritta.ASSETS, "fonts/Lato-Bold.ttf"))
			val font = lato.deriveFont(24f)
			graphics.color = Color(255, 251, 0)
			graphics.font = font

			fun drawTextCentralizedNewLines(text: String, startAtX: Int, startAtY: Int) {
				var startAtX = startAtX
				var startAtY = startAtY

				val splitInput1 = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }
				var input1FitInLine = ""

				for (split in splitInput1) {
					val old = input1FitInLine

					input1FitInLine += split

					println("${startAtX - (graphics.getFontMetrics(font).stringWidth(old) / 2)}")
					if (0 >= startAtX - (graphics.getFontMetrics(font).stringWidth(input1FitInLine) / 2) || startAtX + (graphics.getFontMetrics(font).stringWidth(input1FitInLine) / 2) >= terminatorAnime.width) {
						println((graphics.getFontMetrics(font).stringWidth(old)))

						val drawAtX = startAtX - (graphics.getFontMetrics(font).stringWidth(old) / 2)
						graphics.drawStringWithOutline(old, drawAtX, startAtY, Color.BLACK, 2)
						startAtY += 26
						input1FitInLine = ""
						input1FitInLine += split
					}
				}

				val drawAtX = startAtX - (graphics.getFontMetrics(font).stringWidth(input1FitInLine) / 2)
				graphics.drawStringWithOutline(input1FitInLine, drawAtX, startAtY, Color.BLACK, 2)
			}

			val centerInput1X = 98
			val centerInput1Y = 138
			val centerInput2X = 286
			val centerInput2Y = 254

			drawTextCentralizedNewLines(input1, centerInput1X, centerInput1Y)
			drawTextCentralizedNewLines(input2, centerInput2X, centerInput2Y)

			sendImage(JVMImage(terminatorAnime), "terminator_anime.png")
		}
	}
}