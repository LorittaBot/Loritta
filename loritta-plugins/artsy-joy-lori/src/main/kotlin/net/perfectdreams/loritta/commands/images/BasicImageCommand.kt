package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.arguments
import java.io.File
import javax.imageio.ImageIO

open class BasicImageCommand(
		labels: Array<String>,
		category: CommandCategory,
		val descriptionKey: String,
		val sourceImageFile: String
) : LorittaCommand(labels, category) {
	val template by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, sourceImageFile)) }

	override fun getDescription(locale: BaseLocale) = locale[descriptionKey]

	override fun getUsage(locale: BaseLocale) = arguments {
		argument(ArgumentType.IMAGE) {}
	}

	override val needsToUploadFiles = true
}