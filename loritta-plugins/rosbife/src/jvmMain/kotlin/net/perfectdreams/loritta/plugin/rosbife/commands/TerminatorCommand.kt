package net.perfectdreams.loritta.plugin.rosbife.commands

import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.rosbife.utils.GraphicsUtils
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File

object TerminatorCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("terminator", "animeterminator", "terminatoranime")
	) {
		description { it["commands.images.terminator.description"] }

		examples {
			it.getList("commands.images.terminator.examples")
		}
		usage {
			argument(ArgumentType.TEXT) {}
		}

		needsToUploadFiles = true

		executes {
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

			val centerInput1X = 98
			val centerInput1Y = 138
			val centerInput2X = 286
			val centerInput2Y = 254

			GraphicsUtils().drawTextCentralizedNewLines(
				graphics,
				terminatorAnime,
				input1,
				centerInput1X,
				centerInput1Y
			)

			GraphicsUtils().drawTextCentralizedNewLines(
				graphics,
				terminatorAnime,
				input2,
				centerInput2X,
				centerInput2Y
			)

			sendImage(JVMImage(terminatorAnime), "terminator_anime.png")
		}
	}
}