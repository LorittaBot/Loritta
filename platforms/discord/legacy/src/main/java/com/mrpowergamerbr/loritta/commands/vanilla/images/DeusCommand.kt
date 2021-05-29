package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.image.BufferedImage
import java.io.File

class DeusCommand : AbstractCommand("god", listOf("deus"), CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.god.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
		val template = readImage(File(Loritta.ASSETS + "deus.png")) // Template

		val scaled = contextImage.getScaledInstance(87, 87, BufferedImage.SCALE_SMOOTH)
		template.graphics.drawImage(scaled, 1, 1, null)

		context.sendFile(template, "deus.png", context.getAsMention(true))
	}
}