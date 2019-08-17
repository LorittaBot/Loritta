package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.commands.notNullImage
import java.awt.image.BufferedImage
import kotlin.contracts.ExperimentalContracts

open class BasicSkewedImageCommand(
		labels: Array<String>,
		category: CommandCategory,
		descriptionKey: String,
		sourceImageFile: String,
		vararg val corners: Corners
) : BasicImageCommand(labels, category, descriptionKey, sourceImageFile) {
	@ExperimentalContracts
	@Subcommand
	suspend fun run(context: LorittaCommandContext, locale: BaseLocale) {
		val contextImage = notNullImage(context.getImageAt(0), context)

		val image = BufferedImage(template.width, template.height, BufferedImage.TYPE_INT_ARGB)
		val graphics = image.graphics

		corners.forEach {
			// Nós iremos clonar a imagem para que ela fique em formato ARGB, isso evita problemas de transparência
			val clonedContextImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)
			clonedContextImage.graphics.drawImage(contextImage, 0, 0, null)

			val skewed = LorittaImage(clonedContextImage)

			skewed.resize(template.width, template.height)

			// skew image
			skewed.setCorners(
					it.upperLeftX, it.upperLeftY,
					it.upperRightX, it.upperRightY,
					it.lowerRightX, it.lowerRightY,
					it.lowerLeftX, it.lowerLeftY
			)

			graphics.drawImage(skewed.bufferedImage, 0, 0, null)
		}

		graphics.drawImage(template, 0, 0, null) // Desenhe o template por cima!

		context.sendFile(image, sourceImageFile, context.getAsMention(true))
	}

	class Corners(
			val upperLeftX: Float, val upperLeftY: Float,
			val upperRightX: Float, val upperRightY: Float,
			val lowerRightX: Float, val lowerRightY: Float,
			val lowerLeftX: Float, val lowerLeftY: Float
	)
}