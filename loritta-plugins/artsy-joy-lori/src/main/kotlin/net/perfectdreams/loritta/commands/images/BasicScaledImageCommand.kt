package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.commands.notNullImage
import java.awt.image.BufferedImage
import kotlin.contracts.ExperimentalContracts

open class BasicScaledImageCommand(
		labels: Array<String>,
		category: CommandCategory,
		descriptionKey: String,
		sourceImageFile: String,
		val scaleXTo: Int,
		val scaleYTo: Int,
		val x: Int,
		val y: Int
) : BasicImageCommand(labels, category, descriptionKey, sourceImageFile) {
	@ExperimentalContracts
	@Subcommand
	suspend fun run(context: LorittaCommandContext, locale: BaseLocale) {
		val contextImage = notNullImage(context.getImageAt(0), context)

		val base = BufferedImage(template.width, template.height, BufferedImage.TYPE_INT_ARGB)

		val scaled = contextImage.getScaledInstance(scaleXTo, scaleYTo, BufferedImage.SCALE_SMOOTH)

		base.graphics.drawImage(scaled, x, y, null)

		base.graphics.drawImage(template, 0, 0, null)

		context.sendFile(base, sourceImageFile, context.getAsMention(true))
	}
}