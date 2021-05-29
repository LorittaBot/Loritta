package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaImage
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DrawnMaskCommand : AbstractCommand("drawnmasksign", listOf("drawnmaskplaca"), category = CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "drawn_mask_placa.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData(
			"commands.command.drawnmasksign.description",
			listOf(
					LocaleStringData("Drawn Mask")
			)
	)
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val base = BufferedImage(405, 550, BufferedImage.TYPE_INT_ARGB)
		val scaled = contextImage.getScaledInstance(405, 550, BufferedImage.SCALE_SMOOTH).toBufferedImage()

		val transformed = LorittaImage(scaled)
		transformed.setCorners(167f, 320f,
				367f, 302f,
				387f, 410f,
				179f, 436f)

		val transformedSignImage = transformed.bufferedImage
		val clippedSignImage = BufferedImage(405, 550, BufferedImage.TYPE_INT_ARGB)
		val clippedSignGraphics = clippedSignImage.graphics

		// Para ficar a imagem perfeitamente na mão do Luca, vamos fazer uns snip snip nela!
		val path = Path2D.Double()
		path.moveTo(167.0, 320.0)
		path.lineTo(355.0, 303.0)
		path.lineTo(364.0, 306.0)
		path.lineTo(369.0, 319.0)
		path.lineTo(386.0, 401.0)
		path.lineTo(386.0, 404.0)
		path.lineTo(383.0, 408.0)
		path.lineTo(377.0, 411.0)
		path.lineTo(177.0, 437.0)
		path.closePath()

		clippedSignGraphics.clip = path
		clippedSignGraphics.drawImage(transformedSignImage, 0, 0, null)
		base.graphics.drawImage(clippedSignImage, 0, 0, null)
		base.graphics.drawImage(TEMPLATE, 0, 0, null)

		context.sendFile(base, "drawn_mask_placa.png", context.getAsMention(true))
	}
}