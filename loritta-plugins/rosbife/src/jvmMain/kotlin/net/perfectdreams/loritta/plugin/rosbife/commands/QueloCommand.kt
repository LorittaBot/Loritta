package net.perfectdreams.loritta.plugin.rosbife.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.ImageUtils
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.rosbife.utils.GraphicsUtils
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File

object QueloCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("quelo")
	) {
		description { it["commands.images.quelo.description"] }

		examples {
			it.getList("commands.images.quelo.examples")
		}

		usage {
			argument(ArgumentType.TEXT) { }
		}

		needsToUploadFiles = true

		executes {
			// TODO: Multiplatform
			val args = args.joinToString(" ")

			if (args.length < 1) {
				explain()
				return@executes
			}

			val mppQuelo = loritta.assets.loadImage("quelo.png", loadFromCache = false)
			val template = (mppQuelo as JVMImage).handle as BufferedImage

			val graphics = template.createGraphics()

			graphics.enableFontAntiAliasing()
			val lato = Font.createFont(Font.TRUETYPE_FONT, File(Loritta.ASSETS, "fonts/Lato-Bold.ttf"))
			val font = lato.deriveFont(24f)
			graphics.color = Color.BLACK
			graphics.font = font

			val centerInputX = 184
			val centerInputY = 290

			GraphicsUtils().drawTextCentralizedNewLines(
				graphics,
				template,
				"${locale["commands.images.quelo.template"]} $args",
				centerInputX,
				centerInputY
			)

			sendImage(JVMImage(template), "quelo.png")
		}
	}
}